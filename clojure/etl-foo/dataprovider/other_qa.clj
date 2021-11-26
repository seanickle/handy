

(comment
  "Try out ln/pull-async-wrapper-v1 the asynchronous variety."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['etl-foo.dataprovider.http-utils :as 'hu]
           ['org.httpkit.client :as 'http]
           ['etl-foo.dataprovider.common :as 'common]
           )
  (let
    [app {:FirstName "Blah"}
     url-profiles [
      "config/other_hmm.edn"
      "config/other_hmm.edn"
      ]]
    (def out-chan (
                   ln/pull-async-wrapper-v1
                   hu/fake-http-call-2
                   url-profiles
                   app))
    (println "looking at the channel: " (<!! out-chan))
    (println "? " (<!! out-chan))
    (println "? " (<!! out-chan))
    ; will block if all results have not yet finished
    ))

(comment
  "quick profile map thing"
  (let
    [url-profiles [
                   "config/other_hmm.edn"
                   "config/other_hmm.edn"
                   ]
     profiles (map #(common/read-profile %)
                   url-profiles)
     ]
    profiles)

  )

(comment
  "Try out ln/pull-async-wrapper-v1 the asynchronous variety.
  but with actual http/post against an envt this time.."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['org.httpkit.client :as 'http]
           ['etl-foo.dataprovider.common :as 'common]
           ['clojure.core.async
            :refer ['>! '<! '>!! '<!! 'go 'chan
                    'alts! 'alts!! 'timeout]])
  (let
    [app {:FirstName "Hi"
            }
     url-profiles [
      "config/other_hmm.edn"
      ;"config/other_hmm.edn"
      ]]
    (def out-chan (
                   ln/pull-async-wrapper-v1
                   http/post
                   url-profiles
                   app))
    ;(println "looking at the channel: " (<!! out-chan))
    ; return channel...
    )
  )


(comment
  "Try out ln/pull-async-wrapper-v2 the asynchronous variety."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['org.httpkit.client :as 'http]
           ['etl-foo.dataprovider.http-utils :as 'hu]
           ['etl-foo.dataprovider.common :as 'common]
           )
  (let
    [app {:FirstName "Blah"}
     url-profiles [
      "config/other_hmm.edn"
      "config/other_hmm.edn"
      ]]
    (def results (
                   ln/pull-async-wrapper-v2
                   hu/fake-http-call-3
                   url-profiles
                   app))
    ))

(comment
  "Try out ln/pull the asynchronous variety.
  (ln/pull formerly called ln/pull-async-wrapper-v3)
  This test uses ln/pull , which attempts to re-try for results
  in the channel with errors.
  And using fake-http-call-4 which is a mock http func
  which has a random probability of failure."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['etl-foo.dataprovider.http-utils :as 'hu]
           ['org.httpkit.client :as 'http]
           ['etl-foo.dataprovider.common :as 'common]
           )
  (let
    [app {:FirstName "Blah"}
     url-profiles [
      "config/other_hmm.edn"
      "config/other_hmm.edn"
      "config/other_id.edn"]]
    (def results (
                   ln/pull
                   hu/fake-http-call-4
                   url-profiles
                   app))
    (println "total calls " (:attempts-total results))
    (common/gather-stats-from-pull-attempts (:results results))
    )

  "Real call against other staging."
  (let
    [app {:FirstName "J"
        }
     url-profiles [
                   "config/other_hmm.edn"
                   "config/other_hmm.edn"
                   "config/other_id.edn"]]
    (def results2 (
                  ln/pull
                  http/post
                  url-profiles
                  app))
    (println "total calls " (:attempts-total results2))
    (def stats (common/gather-stats-from-pull-attempts (:results results2)))
    (println "stats.." stats))

  )


(comment
  "Try out ln/pull-sync synchronous variety."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['etl-foo.dataprovider.http-utils :as 'hu]
           ['org.httpkit.client :as 'http])
  (let
    [app-1 {:FirstName "J"
            :Zip "75801"
            }]
    (def result (ln/pull-sync http/post app-1)))
  (let
    [
     app {:FirstName "Blah"}]
    (ln/pull-sync hu/fake-http-call-2 app)))

(comment
  "basic callback"
  (let [callback-fn #(+ 5 %)
        ]
    (callback-fn 10))

  "future w callback func"
  (defn use-callback-when-done
    [callback-fn]
    (future (callback-fn (+ 4 5))))

  (def output (use-callback-when-done #(println "printings.. " % " .end")))

  "use a go put onto a channel callback..."
  (require
    ['clojure.core.async
     :refer ['>! '<! '>!! '<!! 'go 'chan
             'alts! 'alts!! 'timeout]])
  (let [
        my-results-chan (chan)
        callback-fn #(go (>! my-results-chan %))
        output (use-callback-when-done callback-fn)]
    (println "looking at the channel: " (<!! my-results-chan))
    (println "done..")))


(comment
  "Test out an http-kit timeout , using really short timeout"
  (require
    ['org.httpkit.client :as 'http])

  (let
    [options {:timeout 1}
     url "http://www.hmm.com"
     ]
    (def vout @(http/get url options)))

  "Testing to see if callback is used on error.."
  (let
    [options {:timeout 1}
     url "http://www.hmm.com"
     callback-fn #(println "callback yay! ____" % "_____")
     ]
    (def vout (http/get url options callback-fn))
    )

  "Test if username/pass in options is also returned..."
  "Answer is yes."
  (let
    [
     options {:headers {"Content-Type" "text/xml"}
                :basic-auth ["butterfly" "effect"]
                :body "mybody my temple."
              :store-attempts 1
              :timeout 1}
     url "http://www.hmm.com"
     callback-fn #(println "callback yay! ____" % "_____")]
    (def vout @(http/get url options))
    )

  "simple cloj while loop which uses an atom vector"
  (let [results-atom (atom [])]
    (while
      (< (count @results-atom) 3)
      (do
        (println "doing")
        (swap! results-atom conj "hi")
        (println "elements: " (count @results-atom))
          ))
    (println "Done. Now have elements: " (count @results-atom))
    )

  "test bad hostname"
  (let
    [options {}
     url "http://thishostdoesntexist.hmm.com"
     ]
    (def vout @(http/get url options)))

    )

(comment
  "assign var based on probability"
  (def v1 (if (< (rand) 0.5) 4 5)) 
  )

(comment
  "mini swap inc test "
  (let [attempts-total (atom 0)]
    ;
    (swap! attempts-total inc)
    (swap! attempts-total inc)
    @attempts-total
    ))

(comment
  "exception handling"
  ;XMLStreamException ParseError
  (try
     (common/xml->json (xml/parse-str "<xml>" ))
      (catch javax.xml.stream.XMLStreamException ex )
      ;(catch javax.xml.stream.XMLStreamException ex "nope")
    )
  )

(comment
  (let [
        {:keys [status headers body error]} {:status 0 :headers 1 :body 3 :error 5 :extra 88}
        ]
    (println status body)
    )
  )

(comment
  "write json out "

 (require ['clojure.data.json :as 'json])
  (json/write-str {:a 1 :b 2})
  )


(comment
  "test -> with consecutive maps"
  (require ['etl-foo.dataprovider.other :as 'ln])

  (let [valid-xml (slurp "test/etl_spark/data/lll-id-fake.xml")
        v [{:error "timeout"}
           {:status 200 :body "foo"}
           {:status 200 :body valid-xml}
           {:status 400 :body "Error"}
           {:error "timeout" :hmm "welp"}
           ]]
    (def vout (->> v
                   (map ln/annotate-http-error)
                   (map ln/annotate-non-2xx-error)
                   )))

  ;
  (let [valid-xml (slurp "test/etl_spark/data/lll-id-fake.xml")
        v [{:error "timeout"}
           {:status 200 :body "foo"}
           {:status 200 :body valid-xml}
           {:status 400 :body "Error"}
           {:error "timeout" :hmm "welp"}]]
    ;[ln/HTTP-ERROR common/XML-PARSE-ERROR nil ln/NON-200-ERROR ln/HTTP-ERROR]
    (def vout (->> v
                   (ln/annotate-results)
                   ; (map #(:error %))
                   )
      )
    )


  ;
  (let [parsed-body (common/xml-json-safe (:body result))]
    (if (and (some? (:body result))
             (some? (:error parsed-body)))
      "blah"
      "meh"
      )
    )

  (require ['etl-foo.dataprovider.other :as 'ln])

  (let [
        valid-xml (slurp "test/etl_spark/data/lll-id-fake.xml")
        valid-xml-rv (slurp "test/etl_spark/data/lll-hmm-fake.xml")
        d {:status 200 :body valid-xml}
           ]
    ;[ln/HTTP-ERROR common/XML-PARSE-ERROR nil ln/NON-200-ERROR ln/HTTP-ERROR]
    (def vout (->> d
                   (ln/annotate-results)
                   ; (map #(:error %))
                   )
      )
    )


  )

(comment
  "Flattening playing around."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['clojure.string :as 'cs]
           ['clojure.data.json :as 'json])

  (let [
        ; valid-xml-rv (slurp "test/etl_spark/data/lll-hmm-fake.xml")
        valid-xml-rv
        (slurp
          (str (common/local-output-dir)
               "samples/other_hmm_327137-1497039716.xml"))
        v [{:status 200 :body valid-xml-rv :opts {}}]
        rv-prefix "hmm_attributes__"

        vout (->> v (ln/annotate-results))

        ]
    (def rv-annotated-map (ln/flatten-hmm-map (first vout)))

    (spit (str (common/local-output-dir)
               "2017-12-21-rv-out.json"
               )
          (json/write-str rv-annotated-map))

    (def  rv-map (ln/get-ln-data-guts (first vout) :hmm2ResponseEx))
    (def alerts-vec (:Alerts rv-map))

    (def alerts-out-map (cond
                         (list? alerts-vec) (apply conj (map
                                            #(ln/extract-alerts % "RV")
                                            alerts-vec))
                         (map? alerts-vec) (ln/extract-alerts alerts-vec "RV")
                         :else {})
      )
    )

  "id play"
  (let [valid-xml
        (slurp "test/etl_spark/data/lll-id-fake.xml")
        
        v [{:status 200 :body valid-xml :opts {}}]
        vout (->> v (ln/annotate-results))
        annotated-map (ln/flatten-id-map (first vout))
        ]
    (def  id-map (ln/get-ln-data-guts (first vout) :InstantIDResponseEx))
    )
  )

(comment
  "mongo starter"
 
  (require ['monger.core :as 'mg]
           ['monger.collection :as 'mc]
           )
  (import [com.mongodb MongoOptions ServerAddress]) 
  (import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern])
  (let [conn (mg/connect)
        db   (mg/get-db conn "monger-test")
        coll "documents"]
    ;  find..
    ;(def dbconn1 (mc/find db coll {:name "Fobx"}))

    (def o1 
       (mc/find-map-by-id db coll (ObjectId. "4fea999c0364d8e880c05150")) 
      )
    )
 
    ;; given host, given port
    ; (let [conn (mg/connect {:host "127.0.0.1" :port 27017})])
  )

(comment
  "use mongo.clj in this project"
  
  (require ['etl-foo.dataprovider.mongo :as 'mongo])

  (def oids1 (mongo/quick-test))
  
  )


(comment
  "do local xml foo
  
  
 other_id_100003-1465538874.xml 
  "

  (require ['etl-foo.dataprovider.other :as 'ln])

  (let [local-file-detail-vec
        [{:filepath
          (str (common/local-output-dir)
               "samples/other_hmm_327137-1497039716.xml"
               )
          }
         {:filepath
          (str (common/local-output-dir)
               "samples/other_id_327137-1497039713.xml"
               )
          }]] 
    (->>
      local-file-detail-vec
      (map #(:filepath %))
      (map ln/get-filename-from-local-path)
      (map ln/make-metadata-from-filename)))

  ; just one...
  (let [one-detail 
        {:filepath
         (str (common/local-output-dir)
              "samples/other_hmm_327137-1497039716.xml")
          }]
    (-> one-detail
      #(:filepath %)
      ln/get-filename-from-local-path
      ln/make-metadata-from-filename))
    

  (require ['etl-foo.dataprovider.other :as 'ln])
  (let [local-file-detail-vec
        [{:filepath
          (str (common/local-output-dir)
          "samples/other_hmm_327137-1497039716.xml"
               )
          }
         {:filepath
          (str (common/local-output-dir)
               "samples/other_id_327137-1497039713.xml")
          }]]

    (def out-vec
      (ln/do-local-xml-mongo-migration
      local-file-detail-vec))
    )

  "test ->"
  (let [a 9]
    (-> a
        (+ 2)
        (* 2)))


  "manual stuff ln/do-local-xml-mongo-migration  , 
  because not working."
  (def local-file-detail-vec
        [{:filepath
          (str (common/local-output-dir)
               "samples/other_hmm_327137-1497039716.xml")
          }
         {:filepath
          (str (common/local-output-dir)
               "samples/other_id_327137-1497039713.xml")
          
          }])

  (def more-detail-vec (map ln/make-data-thing-from-local-path local-file-detail-vec))


  (def parsed-vec (map common/annotate-xml-parse-error
                            more-detail-vec))


  (def records (map ln/flatten-main parsed-vec))

  )


(comment
  "full run test"
  "NOTE: hmm maybe should have all the side-effect funcs passed in..."
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['etl-foo.dataprovider.common :as 'common]
           ['etl-foo.dataprovider.http-utils :as 'hu])

  (let
    [app-data {:FirstName "J"
               :id "555999"
               :userid "44323"}
     http-pull-fn hu/fake-http-call-4  ; replace w/ http/post  to hit staging.
     mongo-write-fn (fn [records]
                      (println "..fake writing mongo")
                      {:wrote-stuff true})
     ]
    (def out (ln/runner-fn app-data 
                  :mongo-write-fn mongo-write-fn
                  :http-fn hu/fake-http-call-4
                  :write-raw-to-s3-fn common/noop-fn
                  :write-log-to-s3-fn common/noop-fn))))

(comment
  "hmm flat qa"
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['etl-foo.dataprovider.http-utils :as 'hu])

  (let [valid-xml
        (slurp "test/etl_spark/data/lll-hmm-fake.xml")
        v [{:status 200 :body valid-xml :opts {}}]
        vout (->> v (ln/annotate-results))
        one-map (first vout)

        hmm-guts (ln/get-ln-data-guts one-map :hmmResponseEx)
        ]
    )

  )

(comment
  "use mongo.clj in this project"
  
  (require ['etl-foo.dataprovider.mongo :as 'mongo]
           ['etl-foo.service.config :refer ['env]]
           ['monger.core :as 'mg]
           ['monger.collection :as 'mc]

           [monger.query :as mq]
           )

  (mongo/db-count)
  
  )

(comment
  "runner foo"
  (defn rand-map [] {:a (rand-int 100) :b (rand-int 100)})
  (defn flatten-foo [x] (if (odd? x) nil (rand-map)))
  (defn okay-fn [x] (rand-map))
  
  (let [http-results-vec [0 1 2 3]
        flattened-results-1 (map flatten-foo http-results-vec)
        flattened-results-2 (map okay-fn http-results-vec)
        per-product-metadata-vec [{:m 1}{:m 2}{:m 3}{:m 4}]
        final-vec-1 (map conj flattened-results-1 per-product-metadata-vec)
        final-vec-2 (map conj flattened-results-2 per-product-metadata-vec)
        ]
    (prn "flattened-results-1" flattened-results-1)
    (prn "flattened-results-2" flattened-results-2)
    {:final-vec-1 final-vec-1
          :final-vec-2 final-vec-2})
  
  
  )

(comment
  "Some more quick ln/pull QA..
  "
  (require ['etl-foo.dataprovider.other :as 'ln]
           ['org.httpkit.client :as 'http]
           ['etl-foo.service.config :refer ['env]]
           ['etl-foo.dataprovider.common :as 'co]
           )
  (let
    [app {}
     profile {:provider "other" :product-name "id"}
     app-data (co/stage-data-hack app profile)
     url-profiles [:other-id ]]
    (def pull-results (
                  ln/pull
                  http/post
                  url-profiles
                  app-data)))
  )



(comment
  "quick http kit qa... ssl tls.. "
  
(require ['org.httpkit.client :as 'http]
         ['etl-foo.dataprovider.common :as 'co]
         ['etl-foo.dataprovider.other :as 'ln]
         ['etl-foo.service.config :refer ['env]])
  
  (let
    [profile (co/read-profile :other-id)
     {:keys [url]} profile
     ;request (co/render-request app-data profile)
     options (ln/make-ln-http-options profile {})]
    (println "url" url)
    (println "options" options)
    (def vout (http/get url options)))
  )


