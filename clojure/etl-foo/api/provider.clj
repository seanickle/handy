

(ns etl-foo.api.provider
  (:require [clojure.tools.logging :as log]
            [etl-foo.service.config :refer [env]]
            [etl-foo.dataprovider.entry :as entry]
            [etl-foo.dataprovider.http-utils :as hu]
            [etl-foo.dataprovider.common :as common]
            [etl-foo.service.mongodb :refer [mongodb]]
            [etl-foo.dataprovider.blahsentry :as blsentry] 
            [mount.core :as mount]
            ))

(defmulti provider-etl (fn [p & _] p))

(defmethod provider-etl :cool
  [_ arg]
  ;; call cool
  (log/info :cool arg))

(defmethod provider-etl :other-okay
  [_ arg]
  ;; call cool
  (log/info :other-okay arg))



(defn pull-data
  "Pull data from all providers"
  [app-data]
  (clojure.pprint/pprint (select-keys app-data
                                      [:FirstName
                                       :LastName
                                       :id
                                       :userid
                                       :providers]))

  (let [fail-map {:other {:id {:error-p 0 :time 0}
                               :hmmw {:error-p 0}
                               :income {:error-p 0}}
                  :cool {:cool-clm {:error-p 0}
                            :clm {:error-p 0}}}]

    (try 

      (entry/main-runner
        app-data
        )
      (catch Exception e
        (blsentry/throw-sentry :message "main-runner fail" :ex e)
        false))))



