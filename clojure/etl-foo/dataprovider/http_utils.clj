(ns etl-foo.dataprovider.http-utils
  (:require
    [clojure.java.io :as io]
    [clojure.test :as ct]
    ))

(defn fake-http-call
  [url options]
  (let [
        _ (Thread/sleep 1000)
        fake-body (slurp (io/resource "test/etl_foo/data/other-id-fake.xml"))
        fake-response {:status 201 :headers {:hi "blah"} :body fake-body
                       :url url}]
    ; return a future so that the (pull) func. can deref it.
    (future fake-response)))

(defn fake-http-call-2
  [url options callback-fn]
  (let [fake-response {:status 201 :headers {:hi "blah"} :body "thanks"
                     :url url}]
    ; return a future so that the (pull) func. can deref it.
    (future (callback-fn (do
                            (println "starting " url)
                            (Thread/sleep 100)
                            (println "done " url)
                            fake-response)))))

(defn fake-http-call-3
  [url options callback-fn]
  (let
    [fake-body (slurp (io/resource  "test/etl_foo/data/other-id-fake.xml"))
     fake-response {:status 201 :headers {:hi "blah"} :body fake-body
                  :url url}]
    ; return a future so that the (pull) func. can deref it.
    (future (callback-fn (do
                            (println "starting " url)
                            (Thread/sleep 100)
                            (println "done " url)
                            fake-response)))))

(defn fake-http-body-per-product
  [product-name]
  (cond
    (= product-name "id")
    (slurp (io/resource "test/etl_foo/data/other-id-fake.xml"))

    (= product-name "okayhmm")
    (slurp (io/resource "test/etl_foo/data/other-okayhmm-fake.xml"))

    (= product-name "hmm")
    (slurp (io/resource "test/etl_foo/data/other-hmm-fake.xml"))

    (= product-name "cool-clm")
    (slurp (io/resource "test/etl_foo/data/cool_fake1.xml")))
  )

(defn fake-http-call-4
  "has a 0.5 probability of failure...
  Also unlike v-3, this one will return a fake body according
  to the :product-name passed in the options."
  [url options & args]
  (let
    [callback-fn (first args)
     product-name (:product-name options)
     _ (ct/is (some? product-name))
     fake-body (fake-http-body-per-product product-name)
     base-response {:headers {:date "blahdate"}
                    :body fake-body ;:url url
                    :opts (conj options {:url url :method "foo"})}
     fake-response (if (< (rand) 0.5)
                       (conj base-response {:error "crap error"})

                       (conj base-response {:status 201}))]

    (if 
      (some? callback-fn)

      (future (callback-fn (do
                             (println "starting " url)
                             (Thread/sleep 100)
                             (println "done " url)
                             fake-response)))

      ; otherwise, just return that response.
      (future fake-response))))

(defn fake-http-5
  "has a Default 0.5 probability of failure...
  But the probability of failure can be passed in, unlike v-4."
  [failure-prob-map]
  (fn [url options & args]
    (let
      [callback-fn (first args)
       {:keys [product-name provider]} options
       _ (ct/is (some? product-name))
       failure-prob (get-in failure-prob-map
                            (map keyword
                                 [provider product-name :error-p])
                            0)
       wait-time (get-in failure-prob-map
                            (map keyword
                                 [provider product-name :time])
                            0)

       fake-body (fake-http-body-per-product product-name)
       base-response {:headers {:date "blahdate"}
                      :body fake-body
                      :opts (conj options {:url url :method "foo"})}
       fake-response (if (< (rand) failure-prob)
                       (conj base-response {:error "crap error"})

                       (conj base-response {:status 201}))]

      (if 
        (some? callback-fn)

        (future (callback-fn (do
                               (println "starting " url)
                               (Thread/sleep wait-time)
                               (println "done " url)
                               fake-response)))

        ; otherwise, just return that response.
        (future fake-response))) 
    ))

