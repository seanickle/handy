


(ns etl-foo.dataprovider.migrate-util
  (:require [amazonica.aws.s3 :as ss3]
            [etl-foo.dataprovider.mongo :as mongo]
            [etl-foo.dataprovider.s3 :as s3]
            [etl-foo.service.config :refer [env]]
            [etl-foo.dataprovider.common :as common]
            [clojure.test :as ct]
            [cheshire.core :as json]
            [etl-foo.dataprovider.other :as ln]
            [etl-foo.dataprovider.cool :as cl]
            [etl-foo.dataprovider.blahsentry :as blsentry]
            ))


(defn make-other-filename-prefix
  "

  "
  [api-name id]
  (str "other_" api-name "_" id "-"))

(defn make-cool-filename-prefix
  "
  cool.xml"
  [id]
  (str "cool_" id "-"))

(defn make-keys-for-migration
  "this func knows how to make s3 filename key prefixes
  and look around on s3 for valid filenames."
  [data-provider ids]
  (let [prefixes (cond
                   (= data-provider "prov-cool")
                   (concat
                     (map #(make-ln-filename-prefix "id" %) ids)
                     (map #(make-ln-filename-prefix "cool" %) ids)

                     )

                   (= data-provider "cool")
                   (map #(make-other-filename-prefix %) ids)

                   :else nil)

        matches-vec
        (->>
          prefixes
          (map #(common/noop-fn; ss3/list-objects-v2
             {:bucket-name (env :s3-migrate-bucket)
              :prefix %}))
          (map :object-summaries)
          (map first)
          (map :key))
        matches-file-paths-vec
        (map #(conj {:filepath % }) matches-vec)

        nil-mask (map nil? matches-vec)
        nil-prefixes (common/filter-by-mask prefixes nil-mask)

        all-keys (concat matches-file-paths-vec)
        non-null-out
        (filter
          #(some? (:filepath %))
          all-keys)]
    {:nil-prefixes nil-prefixes
     :detail-vec non-null-out}
     ))

(defn make-metadata-from-filename
  "fork of make-metadata-from-filename-ln"
  [filename]
  (let
    [cool-regex #"([^_]{1,})_([^_]{1,})_([0-9]{1,})-([0-9]{1,})\.xml"
     other-regex #"([^_]{1,})_([0-9]{1,})-([0-9]{1,})\.xml"
     cool-match (first (re-seq cool-regex filename))
     other-match (first (re-seq other-regex filename))
     out-dict (cond
                (some? cool-match)
                (let [match cool-match]
                  {:provider (nth match 1);"cool"
                   :product-name (nth match 2);"id"
                   :id (nth match 3)
                   :timestamp (nth match 4)
                   :keyval-name "cool_key"}
                  )

                (some? other-match)
                (let [match other-match]
                  {:provider (nth match 1);"other"
                   :id (nth match 2)
                   :timestamp (nth match 3)
                   :keyval-name "other_key"}))]
    out-dict))

(defn get-filename-from-local-path
  "Either retrieve the file from /some/kind/of/path
  or just return what was passed in otherwise"
  [local-path]
  (let [_ (ct/is (some? local-path))
        matches
        (re-seq #"(\/[^\/]{1,}){1,}\/([^\/]{1,})" local-path)
        match (first matches)
        ]
    (if (some? match)
      (nth match 2)
      local-path)))

(defn make-data-thing-from-path
  [file-detail slurpish-fn]
  (let [x file-detail
        filepath (:filepath x)
        _ (ct/is (some? filepath) "local filepath is nil")
        metadata-dict
        (-> filepath
            (get-filename-from-local-path)
            (make-metadata-from-filename))
        xml-data (slurpish-fn filepath)
        ]
    {:body xml-data
     :opts metadata-dict}
    ))


(defn make-data-thing-from-local-path
  [file-detail]
  (make-data-thing-from-path
    file-detail
    slurp))

(defn make-http-response-thing-from-s3-path
  "Read file at s3 path and annotate with metadata,
  so that output looks as if it came from a (pull) function,
  with {:body xml-data 
        :opts {:provider \"cool\" :id 234}}
(let [s3key \"other-1465538878.xml\"]
    (def foo
      (s3/get-s3-obj s3key))) "
  [file-detail]
  (make-data-thing-from-path
    file-detail
    (partial s3/get-s3-obj ; "aws-s3-blah-bucket"
             (env :s3-migrate-bucket))))

(defn grab-json-data-for-migration
  "In the cases where original data doesnt exist,
  grab the json data from a different bucket."
  [detail]
  (try
    (-> detail
        (:s3-filename)
        ((partial s3/get-s3-obj (env :s3-keyval-migrate-bucket)))
         json/parse-string  ; (json/read-json)
        )
    (catch Exception ex
      (if
        (= "class com.amazonaws.services.s3.model.AmazonS3Exception"
           (str (type ex)))
        {:error "NotFound"}
        (throw (Exception. ex))
        ))))

(defn do-s3-xml-mongo-migration
  "Migrate data from s3 to MongoDB
  
  Expects that the filepaths provided are not nil.
  Will crash otherwise"
  [detail-vec flatten-main-fn]
  (doseq [detail detail-vec]
    (println detail)
    (let [
          faux-http-response-map (make-http-response-thing-from-s3-path detail)
          parsed-elem (common/annotate-xml-parse-error faux-http-response-map)
          app-metadata (common/extract-app-metadata faux-http-response-map)

          record (conj
                   app-metadata
                   (common/make-timestamp-annotation) ;  
                   (flatten-main-fn parsed-elem))

          _ (mongo/insert-many-to-mongo [record])
          ])))

(defn log-migration-result
  "quick helper to write the migration happening json."
  [prefix-data ids data-provider]
  (let [out-fn (str (common/local-output-dir)
                 "migrate-log/"
                 (common/make-timestamp-friendly) "-"
                 data-provider
                 "-migration.json"
                 )
        data {:ids ids
              :prefix-data prefix-data}]
    (spit out-fn (json/generate-string data))))

(defn log-migration-generic
  [data file-prefix]
  (let [out-fn (str (common/local-output-dir)
                 "migrate-log/"
                 (common/make-timestamp-friendly) "-"
                 file-prefix
                 "-.json")]
    (spit out-fn (json/generate-string data))))

(defn xml-s3-migration-runner
  "migrate all   data for ids
  which have corresponding XML on S3.
  
  This func will write to log for when
  XML data has not been found on s3."
  [ids]
  (doseq [data-provider ["cool" "other"]]
    (println "..dataprovider " data-provider)
    (let
      [prefix-details (make-keys-for-migration
                        data-provider
                        ids)
       detail-vec (:detail-vec prefix-details) 
       _ (log-migration-result prefix-details ids data-provider)
       _ (cond
           (= data-provider "cool")
           (do-s3-xml-mongo-migration detail-vec
                                      ln/flatten-main)

           (= data-provider "other")
           (do-s3-xml-mongo-migration detail-vec
                                      cl/flatten-main)
           )
       ])))

(defn make-keyval-json-s3-file-metadata-vec
  "for the json keyval migration,
  make one or more filenames
  so then these filenames will next get read from s3
  
  {:provider (nth match 1);\"cool\"
  :product-name (nth match 2);\"id\"
  :id (nth match 3)
  :timestamp (nth match 4)
  :keyval-name \"cool\"}
  "
  [id data-provider]
  (let [base-map
        {:provider data-provider
         :id id}]
    (cond
      (= data-provider "cool")
      (map #(conj
              base-map
              {:s3-filename (format "%s_%s_%s.json" data-provider % id)
               :product-name %
               :keyval-name "other_all"}
              ) ["riskview" "id" "income"])

      (= data-provider "cool")
      [(conj base-map
             {:s3-filename (format "%s_%s.json" data-provider id)
              :product-name "clm" ; one product right now.
              :keyval-name "cool_all"})])))

(defn make-records-from-json
  [bunch-of-id-provider-tuples]
  (let [s3-file-metadata-vec
        (apply concat
               (map #(let [id (first %)
                           data-provider (last %)]
                       (make-keyval-json-s3-file-metadata-vec
                         id
                         data-provider))
                    bunch-of-id-provider-tuples))

        migration-timestamp-map (common/make-timestamp-annotation)

        json-data-vec
        (map #(conj %
                    (grab-json-data-for-migration
                      (select-keys % [:s3-filename]))
                    migration-timestamp-map
                    (common/make-provenance-annotation "rds-keyval")
                    )
             s3-file-metadata-vec)

        found-json-data-vec (filter #(nil? (:error %)) json-data-vec)

        null-keys-vec (filter #(some? (:error %)) json-data-vec)
        ]
    {:many-records found-json-data-vec
     :not-founds null-keys-vec}
    ))

(defn json-keyval-s3-migration-runner
  "migrate all provider data for ids,
  using alternate s3 bucket, which has json keyval data,
  from RDS KeyVal.
  So this migration doesnt do any flattening, b/c 
  it is already dealing with flattened data."
  [keyval-not-s3-tuples]
  (let [
        ; Partition to make mongo write batched.
        tuple-partitions (partition 1000 1000 [] keyval-not-s3-tuples)
        _ (log-migration-generic tuple-partitions "all")
        ]
    (doseq [one-tuple-partition tuple-partitions]
      (prn "partition: " one-tuple-partition)
      (let [{:keys [many-records not-founds]}
            (make-records-from-json one-tuple-partition)

            _ (log-migration-generic not-founds "not-founds")

            _ (mongo/insert-many-to-mongo many-records)]))))

(defn json-keyval-s3-migration-parallel-runner
  "migrate all provider data for ids,
  using alternate s3 bucket, which has json keyval data,
  from RDS KeyVal.
  fork of json-keyval-s3-migration-runner , but using futures
  So this migration doesnt do any flattening, b/c 
  it is already dealing with flattened data."
  [keyval-not-s3-tuples]
  (let [
        ; Partition to make mongo write batched.
        tuple-partitions (partition 1000 1000 [] keyval-not-s3-tuples)
        num-partitions (count tuple-partitions)
        elements-per-group (quot num-partitions 2)
        groups (partition elements-per-group elements-per-group [] tuple-partitions)

        _ (println "we have num groups: " (count groups))
        futures-list (doall 
                       (map
                         #(let [group %]
                            (println "starting group.")
                            (future
                              (try 
                                (doseq [one-tuple-partition group]
                                  (let
                                    [bounds {:start (first one-tuple-partition)
                                             :end (last one-tuple-partition)}]
                                    (println "start partition: " bounds)
                                    (let [{:keys [many-records not-founds]}
                                          (make-records-from-json one-tuple-partition)]
                                      (if (not (empty? not-founds))
                                        (blsentry/throw-sentry
                                          :message "future parallel runner not-founds"
                                          :data {:not-founds not-founds
                                                 :partition bounds}))
                                      (if (empty? many-records)
                                        (blsentry/throw-sentry
                                          :message "future parallel runner no records"
                                          :data {:many-records many-records
                                                 :partition bounds}))
                                      (mongo/insert-many-to-mongo many-records))
                                    (println "done partition: " bounds)))
                                (catch Exception e
                                  (blsentry/throw-sentry
                                    :message "future parallel runner ex"
                                    :ex e)))
                              ))
                         groups) 
                       )]
    futures-list))

(defn do-local-xml-mongo-migration
  "
  Migrate local XML data provider data to MongoDB.
  detail-vec: vector of maps containing filepaths
  "
  [detail-vec flatten-main-fn] ; insert-fn
  (let [more-detail-vec
        (map make-data-thing-from-local-path
             detail-vec)
        parsed-vec (map common/annotate-xml-parse-error
                            more-detail-vec)
        records (map flatten-main-fn parsed-vec)
        
        insert-blah (mongo/insert-many-to-mongo records)
        ]
    records
    ))
