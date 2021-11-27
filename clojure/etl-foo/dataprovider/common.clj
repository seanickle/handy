
(ns etl-foo.dataprovider.common
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as cs]
            [etl-foo.service.config :refer [env]]
            [cheshire.core :as json]
            [clojure.data.xml :as xml]
            [selmer.parser :refer [render render-file]]
            [clojure.set :as clojset]
            [etl-foo.dataprovider.s3 :as s3]
            [clojure.test :as ct]
            [clojure.data.json :as djson]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            ))


(def XML-PARSE-ERROR "xml-parse-error")
(def MAX-ATTEMPTS-PER-URL 4)
(def HTTP-ERROR "http-error")
(def NON-200-ERROR "non-200-error")

(defn different-keys? [content]
  (when content
    (let [dkeys (count (filter identity (distinct (map :tag content))))
          n (count content)]
      (= dkeys n))))

(defn xml->json [element]
  (cond
    (nil? element) nil
    (string? element) element
    (sequential? element) (if (> (count element) 1)
                            (if (different-keys? element)
                              (reduce into {} (map (partial xml->json ) element))
                              (map xml->json element))
                            (xml->json  (first element)))
    (and (map? element) (empty? element)) {}
    (map? element) (if (:attrs element)
                     {(:tag element) (xml->json (:content element))
                      (keyword (str (name (:tag element)) "Attrs")) (:attrs element)}
                     {(:tag element) (xml->json  (:content element))})
    :else nil))

(defn xml-json-safe
  "Catch parse exceptions and return as error."
  [element]
  (let [error-response {:error "XMLStreamException"}]
    (if (nil? element)
      error-response
      (try 
        (xml->json (xml/parse-str element))
        (catch javax.xml.stream.XMLStreamException ex error-response)))))

(defn safe-trim [v] (cs/trim (str v)))

(defn validate-profile
  [profile]
  (ct/is (some? (:provider profile)))
  (ct/is (some? (:url profile)))
  (ct/is (some? (:template profile)))
  (ct/is (some? (:product-name profile)))
  profile)

(defn read-profile
  ""
  [config]
  (validate-profile
    (get-in env [:provider-data config])))

(defn render-request
  ""
  [app profile]
  (let [{:keys [template cred]} profile]
    (render-file template (merge cred app))))

(defn filter-by-mask
  "Use a [true true false false true] mask,
  only return from in-vec if true in mask
  
#(let [index %
                mask-val %
                value %]
            (if (true? mask-val)
              value
              nil))
  "
  [in-vec mask]
  (filter
    some?
    (map (fn [index value mask-val]
           (if (true? mask-val)
              value
              nil))
         (range (count in-vec)) in-vec mask)))

(defn kw->str
  [kw]
  (subs (str kw) 1))


(defn noop-fn
  "func used as a noop which can take any number of args/kwargs"
  [& args])

(defn simple-flatten
  "take a many-level nested dict and flatten predictably.
  
  Expect that k is a string."
  [k v]
  (cond (or (string? v)
            (number? v))
        {(keyword k) (cs/trim (str v))}

        (and (map? v) (> (count v) 0))
        (let [outv (map #(do
                           (if (map-entry? %)
                             (simple-flatten
                               (str k "__" (kw->str (key %)))
                               (val %))
                             {}))
                        v)
              ]
          (apply conj outv))

        (nil? v)
        {(keyword k) nil}

        :else (do
                {}
                )))


(defn simple-flatten-wrapper
  "
  Do flattening like 
  <main-key>: mainkey
  <prefix>: F
  <obj>:
      {:mainkey
          {:innerkey
              {:foo \"0\" :flarg \"FF007\"}}}
  =>
  {:F__mainkey__innerkey__foo \"0\"
   :F__mainkey__innerkey__flarg \"FF007\"}
  
  "
  [obj main-key prefix]
  ; (str "id_" (kw->str :CurrentName)) (:CurrentName id-map))
  (let [k (str prefix (kw->str main-key))
        v (main-key obj)

        w1 (simple-flatten k v)
        ; _ (prn "w1 " w1)

        ; collapse if needed...
        collapsed (cond
                    (sequential? w1)
                    (apply conj (filter some? w1))

                    (map? w1) w1

                    (nil? w1) {}

                    :else w1)
        ]
    collapsed))


(defn inner-flattenator-helper
  "helper for cases where we want to make a list of dicts"
  [inner-kw obj]
  (let [blah-inner (inner-kw obj)]
    (apply conj
           (map
             #(let
                [k (key %)
                 v (val %)]
                (cond (some? (re-seq #"Attrs" (str k))) {}

                      ; if v looks like 
                      ;(map? v) v

                      :else
                      {k
                       (cs/trim (str v))})
                ) blah-inner))
    ))

(defn outer-list-of-maps-flatten
  "take obj make list of maps"
  [inner-kw obj]
  (map #(inner-flattenator-helper inner-kw %) obj))

(defn kv-nil?
  "key is in map and is a nil"
  [coll k]
  (and (contains? coll k)
       (nil? (k coll))))

(defn make-timestamp
  []
  (str (java.time.LocalDateTime/now)))

(defn make-timestamp-friendly
  []
  (let
    [foo (first 
           (re-seq #"([0-9-]{1,}T\d\d):(\d\d):(\d\d)\.(\d\d\d)"
                   (str (java.time.LocalDateTime/now))))]
    (str (foo 1) (foo 2) "." (foo 3) "." (foo 4))))

(defn make-s3-prefix-date
  []
  (let
    [foo (first 
           (re-seq #"([0-9]{4})-([0-9]{2})-([0-9]{2})"
                   (str (java.time.LocalDateTime/now))))]
    (str (foo 1) "/" (foo 2) "/" (foo 3) "/")))

(defn make-unix-timestamp
  []
  (quot (System/currentTimeMillis) 1000)
  ;(int (/ (.getTime (java.util.Date.)) 1000))
  )

(defn make-timestamp-annotation
  "write map describing timestamp when record
  is being inserted into target database"
  [] ;TODO: or name this "insertion timestamp" ?
  {:migration-timestamp (make-timestamp)})

(defn make-provenance-annotation
  "write map describing origin of this record
  being inserted into target database"
  [provenance]
  (let [applicationkeyval "rds-applicationkeyval"
        s3-xml "s3-xml"
        dataprovider "dataprovider"]
    (cond
      (= provenance applicationkeyval)
      {:data-provenance applicationkeyval}

      (= provenance s3-xml)
      {:data-provenance s3-xml}

      (= provenance dataprovider)
      {:data-provenance dataprovider}

      :else
      (throw (Exception. "Illegal provenance "))
      )))

(defn select-opposite-keys
  "Return map with entries whose keys are not in keyseq
  This is the difference of select-keys would return"
  [a-map keyseq]
  (let [all-map-keys (keys a-map)
        difference-keys (clojset/difference
                          (set all-map-keys)
                          (set keyseq))
        ]
    (select-keys a-map difference-keys)))

(defn annotate-xml-parse-error
  "Annotate XML, but if there is already an error,
  then dont do anything. return original response"
  [result]
  (let [parsed-body (xml-json-safe (:body result))
        original-opts (:opts result)]
    (if (and
          (some? (:body result))
          (some? (:error parsed-body))
          (nil? (:error result)))
      (conj result {:error XML-PARSE-ERROR
                    :opts original-opts})
      (conj result {:parsed-body parsed-body
                    :opts original-opts}))))

(defn extract-app-metadata
  "Take http response thing which has app metadata in :opts
  such as provider and appid.
  and return that as map.
  Reason for doing this: helpful on Mongodb"
  [data]
  (let
    [options (:opts data)]
    (select-keys options [:appid
                          :product-name
                          :provider
                          :keyval-name
                          :timestamp])))

(defn select-app-data
  [app-data]
  (select-keys
    app-data [:FirstName
              :Zip
              :IpAddress
              ]))

(defn parse-json
  "Convert JSON stream to clojure map with clojure style keywords"
  [s]
  (json/parse-stream (io/reader s) keyword))

(defn local-output-dir
  []
  (env :local-output-dir))

(defn write-raw-to-s3
  [& {:keys [appid data-provider product-name content]
      :or {product-name nil}}]
  (ct/is (string? content))
  (let [bucket-name-raw "dataprovider-raw" ; TODO get this from env
        timestamp-unix (make-unix-timestamp)  ; TODO UTC.
        s3-key (if (string? product-name)
                 (str data-provider "_" product-name "_" appid "-" timestamp-unix ".xml")

                 (str data-provider "_" appid "-" timestamp-unix ".xml"))]
    (s3/put-s3-obj bucket-name-raw s3-key content)))

(defn write-log-to-s3
  [& {:keys [appid data-provider product-name content]
      :or {product-name nil}}]
  (let
    [bucket-name (env :s3-general-log-bucket)
     s3-key (str
              (make-s3-prefix-date)
              appid "-" data-provider "-" (make-timestamp-friendly) ".json")
     content-json (json/generate-string content)]
    (s3/put-s3-obj bucket-name s3-key content-json)))

(defn dump-local
  "dump map locally"
  [data aname]
  (let [blah-json (json/generate-string data)
        filename (str (env :local-output-dir)
                      (make-timestamp-friendly)
                      "."
                      aname ".json")]
    (spit filename blah-json)))

(defn annotate-http-error
  [result]
  (let [error-result (:error result)]
    (if error-result
      (conj result {:error HTTP-ERROR
                    :error-detail error-result})
      result)))

(defn annotate-non-2xx-error
  [result]
  (let [status-type (first
                      (str (:status result)))]
  (if (and (not= \2 status-type)
          (some? (:status result)))
    (conj result {:error NON-200-ERROR})
    result)))

(defn annotate-results
  "Take responses from HTTP requests,
  - extract XML
  - and identify error conditions.
  
  Expects:
    [{:status 200 :body valid-xml :opts {}}
    {:status 200 :body valid-xml :opts {}} ]"
  [results]
  (->> results
       (map annotate-http-error)
       (map annotate-non-2xx-error)
       (map annotate-xml-parse-error)
       
       ;  do flattening  elsewhere.
       ))

(defn retry-failed-requests
  "If responses have errors, retry them,
  until MAX-ATTEMPTS-PER-URL is reached."
  [results-chan http-fn callback-fn num-profiles]
  (let [results-atom (atom [])
        all-results-atom (atom [])
        attempts-total (atom 0)]
    (while (< (count @results-atom) num-profiles)
      (do
        (let
          [thing (<!! results-chan)
           options (:opts thing)
           url (get-in thing [:opts :url])
           _ (swap! attempts-total inc)
           _ (swap! all-results-atom conj thing)]

          (if (:error thing)
            (let [num-attempts (inc (:num-attempts options))
                  options-updated (conj options {:num-attempts num-attempts})]

              (if (< (:num-attempts options) MAX-ATTEMPTS-PER-URL)
                (http-fn url options-updated callback-fn)

                (swap! results-atom conj thing)))

            (swap! results-atom conj thing)))))

    {:results @results-atom :attempts-total @attempts-total
     :all-results @all-results-atom}))

(defn gather-stats-from-pull-attempts
  "After running pull
  how many times did we have to try for each url?
  We retry whenever there are errors with the http request."
  [results-vec]
  (map #(let [res %
              opts (:opts res)]
          (conj (select-keys res [:error :status])
                (select-keys opts [:url
                                   :num-attempts
                                   :product-name
                                   :mark-timestamp
                                   :provider])
                {:response-header-date (get-in res [:headers :date])}
                (if (nil? (:error res)) {:success true})
                ))
       results-vec))

(defn filter-by-index [coll idx]
  (map (partial nth coll) idx))

(defn filter-by-mask [coll mask]
  (filter some? (map #(if %1 %2) mask coll)))

(defn make-errors-mask [coll]
  (map #(nil? (:error %)) coll))

(defn stage-data-hack
  "If in test profile, select special stage data.
  Why: blahblah requires different test data per product."
  [data profile]
  (if (some? (env :hack-app-data))
    (let [hack-map (get-in env [:hack-app-data
                                (:provider profile)
                                (:product-name profile)])]
      (if (map? hack-map)
        (conj data hack-map)
        data))
    data))

(defn str->num [x]
  (if (string? x)
    (try (Double/parseDouble x)(catch Exception ex nil))))

(defn str->int-or-double [x]
  (if (string? x)
    (or (try (Integer/parseInt x)(catch Exception ex nil))
        (try (Double/parseDouble x)(catch Exception ex nil)))))

(defn str->bool [x]
  (cond (= x "true") true
        (= x "false") false
        (= x "1") true
        (= x "0") false
        :else nil))

(defn make-numerics
  [inmap special-keys]
  (let [updates-vec (map #(let [n (str->num (% inmap))]
                            (if (number? n){% n}{}))
                         special-keys)
        updates (apply conj updates-vec)]
    (if (not (empty? updates))
      (conj inmap updates)
      inmap)))

(defn make-integers
  [inmap special-keys]
  (let [updates-vec (map #(let [n (str->int-or-double (% inmap))]
                            (if (number? n){% n}{}))
                         special-keys)
        updates (apply conj updates-vec)]
    (if (not (empty? updates))
      (conj inmap updates)
      inmap)))

(defn make-bools
  [inmap special-keys]
  (let [updates-vec (map #(let [n (str->bool (% inmap))]
                            (if (or (true? n)(false? n)){% n}{}))
                         special-keys)
        updates (apply conj updates-vec)]
    (if (not (empty? updates))
      (conj inmap updates)
      inmap)))

