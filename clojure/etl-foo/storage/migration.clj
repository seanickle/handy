(ns etl-foo.storage.migration
  (:require [etl-foo.service.config :refer [env]]
            [etl-foo.service.mongodb :refer [mongodb]]
            [clojure.tools.logging :as log]
            [etl-foo.dataprovider.migrate-util :as mu]
            [cheshire.core :as json]
            [etl-foo.dataprovider.mongo :as mongo]
            [etl-foo.dataprovider.s3 :as s3]
            [mount.core :as mount]))

(defn migrate
  [args]
  (mount/start #'etl-foo.service.config/env
               #'etl-foo.service.mongodb/mongodb
               #'etl-foo.service.aws/cred)

  (let [offset (first args)
        limit (last args)
        s3-bucket-copy-paste "blah-bucket"
        provider-tuples-s3-filename
        (str
         "hmmmmm"
          "/stuff/blah.json")

        provider-tuples-unsorted (->
                                    provider-tuples-s3-filename
                                    ((partial s3/get-s3-obj s3-bucket-copy-paste))
                                    (json/parse-string))

        provider-tuples-all (sort-by
                                    first < provider-tuples-unsorted)

        provider-tuples
        (cond
          (and (number? offset) (number? limit))
          (->> provider-tuples-all
               (take (+ offset limit))
               (drop offset))

          (and (number? offset) (nil? limit))
          (drop offset provider-tuples-all)

          :else provider-tuples-all)
        ]
    (prn "first:" (first provider-tuples)
         "last:" (last provider-tuples))
    (println "counting prov tuples..." (count provider-tuples))

    (mu/json-keyval-s3-migration-parallel-runner
            provider-tuples))

  (clojure.pprint/pprint args))

(defn count-records
  []
  (mount/start #'etl-foo.service.config/env
               #'etl-foo.service.mongodb/mongodb)
  (mongo/db-count))

(defn summarize-records
  []
  (mount/start #'etl-foo.service.config/env
               #'etl-foo.service.mongodb/mongodb)
  (mongo/db-summary))
