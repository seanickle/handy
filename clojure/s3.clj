(ns etl-spark.storage.s3
  (:require [amazonica.aws.s3 :as s3]
            [clojure.core.async :as a]
            [clojure.tools.logging :as log]
            [etl-spark.service.aws :refer [cred s3-channel]])
  (:import [java.io ByteArrayInputStream]))

(defn s3-put-string
  [key data]
  (let [bucket "blahh-bucket"
        bytes (.getBytes data)]
    (s3/put-object
                   :bucket-name bucket
                   :key key
                   :input-stream (ByteArrayInputStream. bytes)
                   :meta-data {:content-type "application/xml"
                               :content-length (count bytes)})))

(defn s3-put-loop
  []
  (a/go-loop []
    (let [data (a/<! s3-channel)
          {key :key content :content} data]
      (s3-put-string key data))
    (recur)))

(defn async-s3-put
  "Async S3 Put"
  [key content]
  (let [data {:key key :content content}]
    (a/go (a/>! s3-channel data))))

(comment
  (async-s3-put "foo1" "<foo><bar>test1</bar></foo>")
  (s3-put-string "foo2" "<foo><bar>test1</bar></foo>")
)
