
(ns etl-spark.dataprovider.other
  (:require
    [etl-spark.dataprovider.common :as co]
    [clojure.data.xml :as xml]
    [clojure.string :as cs]
    [clojure.core.async
     :as a
     :refer [>! <! >!! <!! go chan buffer close! thread
             alts! alts!! timeout]]
    [etl-spark.dataprovider.mongo :as mongo]
    [clojure.test :as ct]
    [etl-spark.dataprovider.s3 :as s3]
    [clojure.data.json :as djson]
    [etl-spark.dataprovider.blahsentry :as blsentry]
    ))

(def MAX-ATTEMPTS-PER-URL 4)
(def HTTP-ERROR "http-error")
(def NON-200-ERROR "non-200-error")

(def sensitive-other-keys
  [:id_sensitive_key])

(defn make-ln-http-options
  [profile request]
  (let
    [{:keys [username password]} (:cred profile)]
    (conj 
      {:headers {"Content-Type" "text/xml"}
       :basic-auth [username password]
       :body request
       :mark-timestamp (co/make-timestamp-friendly)
       :num-attempts 1}
      (select-keys profile [:product-name :provider])
      {:keyval-name "other_all"})))

(defn pull
  "Use given http-fn, trying to handle errors w/ retries,
  up to a max number of retries."
  [http-fn url-profiles app]
  (let
    [results-chan (chan)
     profiles (map #(co/read-profile %)
                   url-profiles)
     num-profiles (count url-profiles)
     callback-fn #(go (>! results-chan %))]

    (doseq [profile profiles]
      (let
        [{:keys [url headers template cred]} profile
         app-data (co/stage-data-hack app profile)
         request (co/render-request app-data profile)
         options (make-ln-http-options profile request)]
        (http-fn url options callback-fn)))

    (let [{:keys [results attempts-total]}
          (co/retry-failed-requests results-chan
                                        http-fn
                                        callback-fn
                                        num-profiles)]
      {:results (co/annotate-results results)
       :raw-results results
       :attempts-total attempts-total})))

(defn extract-attribute
  [obj key-prefix]
  (let [inner (:Attribute obj)
        attr-key (co/safe-trim (:Name inner))
        attr-val (:Value inner)
        prefixed-key (str key-prefix attr-key)]
    {(keyword prefixed-key) (co/safe-trim attr-val)}))




(defn xml-to-mongo
  "Take some xml and metadata.. and throw into mongo..
  
  {:product-name \"blahproduct\" :xml blah-xml} 
  "
  [data metadata-foo]
  (let [
        records [data]
        insert-blah (mongo/insert-many-to-mongo records)
        ])
  )


(defn make-metadata-from-filename-ln
  "
  Take apart the filename we use on s3 based on
  the dataprovider info and foo info.
  
  TODO!! this doesnt work if the product is nil !  "
  [filename]
  ; other_id_100003-1465538874.xml 
  ;
  ; make a timestamp from ctime
  ;In [11]: time.ctime(1465538874)
  ; Out[11]: 'Fri Jun 10 02:07:54 2016'
  ;
  (let [matches
        (re-seq
          #"([^_]{1,})_([^_]{1,})_([0-9]{1,})-([0-9]{1,})\.xml"
          filename)
        match (first matches)
        ]
    (if (some? match)
      {:id (nth match 3)
       :product-name (nth match 2);"id"
       :provider (nth match 1);"other"
       :keyval-name "other_all";"other_all"
       :timestamp (nth match 4)};"blah-timestamp"
      ; otherwise... nada.
      nil))) ; :id :product-name :provider :keyval-name :timestamp

(defn extract-simple-nest
  "Heres a map. Flatten everything underneath it.
  Go 1 or 2 levels deep if available."
  [obj k key-prefix]
  (let [inner (k obj)
        obj-prefix (str (subs (str k) 1) "__")
                        ]
    ;(map #(simple-flatten % key-prefix obj-prefix) inner)
    ))

(defn extract-model-r-indices
  "A model score can have an inner list of Name/Values
  Extract these as well.
  
  This was meant for {:Indices [{:FooIndex {}}]}
  in particular, but most likely this can be adapted to a few other
  XML constructs.
  "
  [input-map outer-wrapper-key model-type prefix inner-keyword]
  (cond (nil? (outer-wrapper-key input-map)) nil
        :else
        (let [
              reasons-vec (outer-wrapper-key input-map)
              key-prefix (co/safe-trim 
                           (str prefix
                              model-type
                              "_"
                              (subs (str outer-wrapper-key) 1)))
              reasons (map #(let [a-map (inner-keyword %)]
                              {
                               (keyword (str key-prefix
                                             "__"
                                             (co/safe-trim (:Name a-map))
                                             ))
                               (:Value a-map)}
                              )
                           reasons-vec)
              reasons-map (apply conj reasons)
              ]
          reasons-map)))

(defn make-inner-compreh-helper
  [r-indicator-obj]
  (let [blah-inner (:RIndicator r-indicator-obj)]
    {(keyword (str "Flag_"
                   (co/safe-trim (str (:RCode blah-inner)))))
     (co/safe-trim (str (:Description blah-inner)))}))

(defn make-compreh-indicators
  [obj output-kw]
  (let [cvi (:ComprehensiveVerificationIndex obj)
        cvi-index-map (if (some? cvi)
                        {:id_cvi__Stuff
                         (co/safe-trim (str cvi))})
        r-indicators (:Indicators obj)

        r-indicator-map-inner
        (cond
          (nil? r-indicators) nil

          (sequential? r-indicators)
          (apply conj
                 (map
                   make-inner-compreh-helper
                   r-indicators))

          (map? r-indicators)
          (make-inner-compreh-helper r-indicators)

          :else nil)

        r-indicator-map (if (some? r-indicator-map-inner)
                             {output-kw
                              r-indicator-map-inner})]
    (cond
      (and (nil? cvi-index-map)
           (nil? r-indicator-map))
      nil

      (nil? cvi-index-map) r-indicator-map

      (nil? r-indicator-map) cvi-index-map

      :else
      (conj r-indicator-map
            cvi-index-map))))


(defn extract-json-field
  "extract the json id_Score_FraudPoint_rIndicators
  field (and possibly others like it).
  This is one of those fields which is being used as a nested json field.
  Hopefully later we can dig into these kinds of fields. For now just try
  to re-create what is there right now."
  [obj key-prefix main-key]
  (let [k1 (str key-prefix
               (co/kw->str main-key))
        k "id_Score_FraudPoint_rIndicators"
        v (main-key obj)]
    {(keyword k) v}))


(defn extract-score
  "extract scores in RV  , or ID."
  [obj key-prefix model-key-separator]
  (cond (nil? obj) nil
        :else
        (let [
              inner (:Model obj)

              model-name (co/safe-trim (:Name inner))  ; TODO check nil?
              model-type-parts (cs/split model-name
                                         (re-pattern model-key-separator))
              model-type (first model-type-parts)
              model-number (subs model-name (count model-type))

              scores-entry (get-in inner [:Scores :Score])
              score-type (co/safe-trim (:Type scores-entry))
              score-value (co/safe-trim (:Value scores-entry))

              r-indices-map (extract-model-r-indices
                                 scores-entry
                                 :Indices ;outer-wrapper-key
                                 model-type
                                 key-prefix
                                 :rIndex)

              r-indicators-blah-map (make-compreh-r-indicators
                ; (:ComprehensiveVerification scores-entry)
                scores-entry
                :id_Score_)
              ]
          (conj
            {(keyword (str key-prefix model-type "_Model_Name")) model-number
             (keyword (str key-prefix model-type "_Score_Type")) score-type
             (keyword (str key-prefix model-type)) score-value

             ; NOTE ...
             }
            r-indices-map 
            r-indicators-blah-map ; TODO make sure this is nil not [nil] if empty.
            ))))

(defn extract-alerts
  "from rview"
  [obj key-prefix]
  (let [inner (:Alert obj)
        code (:Code inner)
        description (:Description inner)
        ]
    {(keyword (str key-prefix code)) description})
  )

(defn get-ln-data-guts
  "This func knows how to reach into the guts
  of a payload
  "
  [data internal-key]
  (get-in data
          [:parsed-body ;TODO maybe not this kw?
           :Envelope 
           :Body 
           internal-key 
           :response 
           :Result]))

(defn ln-make-uniqueid-map
  [data prefix]
  (let [internal-key (cond
                       (= prefix "hmm") :Respons
                       (= prefix "id") :ResponseE)
        data-inside (get-in data
                            [:parsed-body
                             :Envelope 
                             :Body 
                             internal-key 
                             :response])
        unique-id (co/safe-trim (get-in data-inside [:Result :UniqueId]))
        query-id  (co/safe-trim (get-in data-inside [:Header :QueryId]))]
    {(keyword (str prefix "_uniqueid")) unique-id
     (keyword (str prefix "_queryid")) query-id
     :lex_id unique-id
     :transaction_id query-id}))

(defn chronology-complex-flatten
  "Custom flattening for id_chronology_history "
  [data]
  (let
    [outer-kw [:ChronologyHistories]

     internal-tag-names [:ChronologyHistory]

     out-map-vec (map
                #(let [outer-kw %1
                       inner-kw %2]
                   {:id_chronology_history

                    (co/outer-list-of-maps-flatten
                      inner-kw
                      (get-in data
                              [outer-kw]))}   

                   ) outer-kw internal-tag-names)
     output-map (apply conj out-map-vec)]
    output-map))


(defn flatten-rview-map
  " 
  Example...
 Result:AttributesGroup:Attributes => rview_attributes__** 
  "
  [data]
  (let [rv-map (get-ln-data-guts data :rView2ResponseEx)

        ; rview attributes
        rv-attributes-prefix "rview_attributes__"
        attributes-vec (get-in rv-map [:AttributesGroup :Attributes])
        attributes-annotated-map
        (apply conj
               (map #(extract-attribute % rv-attributes-prefix)
                    attributes-vec))

        ; rview scores
        rv-scores-prefix "rview_scores__"
        scores-vec (:Models rv-map)
        ; FIXME ... should test for single and multi-elem <Models> lists
        scores-out-map (apply conj
                              (map
                                #(extract-score % rv-scores-prefix "RV")
                                scores-vec)           
                              )

        ; rview alerts
        rv-alerts-prefix "rview_alerts__"
        alerts-vec (:Alerts rv-map)
        alerts-out-map (cond
                         (list? alerts-vec)
                         (apply
                           conj
                           (map
                             #(extract-alerts % rv-alerts-prefix)
                             alerts-vec))
                         (map? alerts-vec)
                         (extract-alerts alerts-vec
                                         rv-alerts-prefix)
                         :else {})

        unique-id-map (ln-make-uniqueid-map data "rview")
        ]
    (-> (conj
          attributes-annotated-map
          scores-out-map
          alerts-out-map
          unique-id-map)
        (co/make-numerics [:rview_scores__Telecom
                           :rview_scores__BankCard
                           :rview_scores__Auto
                           :rview_scores__ShortTermLending]))))

(defn flatten-income-map
  [data]
  (let
    [income-map (get-ln-data-guts data :rViewResponseEx)
     income-model-internal (get-in income-map [:Models :Model :Scores :Score])]
    (if (some? income-model-internal)
      (let [ 
            income-model-score (co/safe-trim (:Value income-model-internal))
            model-name (:Type income-model-internal)
            model-type-parts (cs/split model-name
                                       (re-pattern "Income"))
            income-model-name (co/safe-trim (second model-type-parts))]
        (-> {:income_scores__Income income-model-score
             :income_scores__Income_Model_Name income-model-name}
            (co/make-numerics [:income_scores__Income ]))))))

(defn make-nap-map [id-map]
  (if-let [obj (:NameAddressfone id-map)]
    {:id_nap_summary__data_type (when-let [x (:Type obj)](co/safe-trim x))
     :id_nap_summary__index_value (when-let [x (:Summary obj)](co/safe-trim x))
     :id_nap_summary__fone_type (when-let [x (:Status obj)](co/safe-trim x))}
    {:id_nap_summary__data_type nil
     :id_nap_summary__index_value nil
     :id_nap_summary__fone_type nil}))

(defn flatten-id-map
  [data]
  (let
    [id-map (get-ln-data-guts data :Respon)

     ; top level nestings...
     ;  id_CurrentName__First	NAVY

     ; id scores
     id-scores-prefix "id_scores__"
     scores-vec (:Models id-map)
     scores-out-map (cond
                      (list? scores-vec)
                      (apply
                        conj
                        (map
                          #(extract-score % id-scores-prefix "FP")
                          scores-vec))
                      (map? scores-vec)
                      (extract-score scores-vec id-scores-prefix "FP")
                      :else {})

     simple-map (apply conj (map
                              #(co/simple-flatten-wrapper
                                 id-map % "id_")
                              [:CurrentName
                               :NewAreaCode
                               :VerifiedInput
                               :Reversefone
                               ]))

     ; misc
     singletons-map (apply conj (map
                                  #(co/simple-flatten-wrapper
                                     id-map % "id_misc__")
                                  [:AddressCMRA
                                   :CommercialAddress
                                   :AddressPOBox
                                   :PassportValidated
                                   :DOBMatchLevel
                                   :DOBVerified]))
     nap-map (make-nap-map id-map)

     ; id_cvi__ComprehensiveVerificationIndex

     ;:id_ComprehensiveVerificationIndex_rIndicators
     comprehensive-r-indicators-map
     (make-compreh-r-indicators
       (:ComprehensiveVerification id-map)
       :id_ComprehensiveVerificationIndex_rIndicators)

     unique-id-map (ln-make-uniqueid-map data "id")
     
     chronology-map (chronology-complex-flatten id-map)]
    (-> (conj
          scores-out-map
          simple-map
          singletons-map
          comprehensive-r-indicators-map
          unique-id-map
          chronology-map
          nap-map)
        (co/select-opposite-keys sensitive-other-keys)
        (co/make-numerics [:id_scores__FraudPoint])
        (co/make-integers [:id_misc__DOBMatchLevel
                           :id_misc__Count])
        (co/make-bools [:id_misc__AddressPOBox
                        :id_misc__Foo
                        :id_misc__AddressCMRA
                        :id_misc__DOBVerified
                        :id_misc__PassportValidated]))))

(defn flatten-main
  "flatten multiplexor for the different products."
  [data] 
  (let [options (:opts data)
        product-name (:product-name options)]
    (try
      (cond
        (some? (:error data))
        {}

        (= product-name "rview")
        (flatten-rview-map data)

        (= product-name "id")
        (flatten-id-map data)

        (= product-name "income")
        (flatten-income-map data)

        :else {})
      (catch Exception e
        (blsentry/throw-sentry :message "ln/flatten-main ex" :ex e
                               :data data)
        {}))))
 
(defn runner-fn
  "Main LN func entry for a new app."
  [input-app-data & {:keys [http-fn
                            mongo-write-fn
                            write-raw-to-s3-fn
                            write-log-to-s3-fn]}]
  (let
    [start-time (co/make-timestamp-friendly)
     app-data (co/select-app-data input-app-data)
     url-profiles [:other-rview
                   :other-income
                   :other-id]
     out (pull
           http-fn ; e.g. http/post
           url-profiles
           app-data)
     http-results-vec (:results out)

     _ (doseq [http-result http-results-vec]
         (let [product-name (get-in http-result [:opts :product-name])]
           (write-raw-to-s3-fn :data-provider "other" :product-name product-name
                               :id (:id input-app-data)
                               :content (:body http-result))))

     app-metadata (conj
                    (select-keys input-app-data [:id :userid])
                    {:provider "other"
                     :timestamp (co/make-timestamp)}
                    (co/make-provenance-annotation "dataprovider"))

     some-stats (co/gather-stats-from-pull-attempts http-results-vec)
     error-mask (co/make-errors-mask http-results-vec)
     http-results-vec-no-errors (co/filter-by-mask http-results-vec error-mask)
     flattened-results (map flatten-main http-results-vec-no-errors)
     not-empty-mask (map #(not (empty? %)) flattened-results)
     flattened-results-filtered (co/filter-by-mask flattened-results not-empty-mask)
     per-product-metadata-vec (map co/extract-app-metadata http-results-vec-no-errors)
     per-product-metadata-vec-filtered (co/filter-by-mask per-product-metadata-vec not-empty-mask)
     final-vec (map conj flattened-results-filtered per-product-metadata-vec)

     timestamp-map (co/make-timestamp-annotation) ; TODO hmm should general also have the :migration-timestamp ?

     records (map #(conj % app-metadata timestamp-map)
                  final-vec)
     
     before-mongo-time (co/make-timestamp-friendly)
     _ (mongo-write-fn records)
     end-time (co/make-timestamp-friendly)
     timestamp-map {:walltimes {:start start-time
                                :before-mongo-time before-mongo-time
                                :end end-time}}
     ]
    ; TODO should be returning error if there was mongo write error..

    (conj app-metadata {:some-stats some-stats}
          timestamp-map)))
