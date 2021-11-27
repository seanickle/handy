(ns etl-foo.dataprovider.entry
  (:require
    [etl-foo.dataprovider.common :as common]
    [etl-foo.dataprovider.okay :as ln]
    [etl-foo.dataprovider.cool :as cl]
    [cheshire.core :as json]
    [etl-foo.dataprovider.http-utils :as hu]
    [org.httpkit.client :as http]
    [etl-foo.dataprovider.mongo :as mongo]
    [clojure.test :as ct]
    [etl-foo.dataprovider.migrate-util :as mu]))

(defn success?
  [result]
  (let [cool-error (some? (:error 
                               (first (get-in result [:cool :some-stats]))
                               ))
        other-okay-error (some true?
                            (map #(some? (:error %))
                                 (get-in result [:okay :some-stats]) 
                                 ))]
    (not (some true? [cool-error other-okay-error]))))

(defn main-runner
  [app-data & {:keys [mongo-write-fn
                      http-fn
                      write-raw-to-s3-fn
                      write-log-to-s3-fn]
               :or {mongo-write-fn mongo/insert-many-to-mongo
                    http-fn http/post
                    write-raw-to-s3-fn common/write-raw-to-s3
                    write-log-to-s3-fn common/write-log-to-s3}
               :as fn-map
               :pre [(some? (:providers app-data))]  ; FIXME not working.
               }]
  (let [start-time (common/make-timestamp-friendly)
        _ (prn "DEBUG: entry/main-runner start: " start-time)
        providers (:providers app-data)
        futures-vec (mapv #(future 
                            (println "starting .." %1)
                            {(keyword %1)
                             (%2 app-data
                                 :http-fn http-fn
                                 :mongo-write-fn mongo-write-fn
                                 :write-raw-to-s3-fn write-raw-to-s3-fn
                                 :write-log-to-s3-fn write-log-to-s3-fn)})

                         providers

                         (map {"okay" ln/runner-fn
                               "cool" cl/runner-fn}
                              providers))
        
        result-details (apply conj (mapv deref futures-vec))
        end-time (common/make-timestamp-friendly)
        timestamp-map {:walltimes {:start start-time
                                   :end end-time}}
        log-map (conj result-details timestamp-map)

        _ (write-log-to-s3-fn :appid (:appid app-data)
                              :content log-map)]
    (prn "DEBUG: entry/main-runner end: " (common/make-timestamp-friendly))
    (success? result-details)))


