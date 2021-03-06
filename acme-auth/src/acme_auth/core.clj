(ns acme-auth.core
  (:require [compojure.core :refer [defroutes ANY POST]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-params]]
            [acme-auth.datasource :refer [get-ds]]
            [acme-auth.bootstrap :refer [create-db seed]]
            [acme-auth.handlers :as handlers]
            [ring.logger :as logger]))




(defn bootstrap []
  (println "Bootstrapping....")
  (let [ds (get-ds)]
    (create-db ds)
    (seed ds)))


(defroutes app-routes
  (ANY "/" [] (fn [req] (println req) "Index of acme auth"))
  (POST "/create-auth-token" [] handlers/create-auth-token)
  (POST "/refresh-auth-token" [] handlers/refresh-auth-token)
  (POST "/invalidate-refresh-token" [] handlers/invalidate-refresh-token))


(defn wrap-datasource [handler]
  (fn [req]
      (handler (assoc req :datasource (get-ds)))))

(defn wrap-config [handler]
  (fn [req]
    (handler (assoc req :auth-conf {:privkey "auth_privkey.pem"
                                    :pubkey "auth_pubkey.pem"
                                    :passphrase "secret-key"}))))

(def app
  (-> app-routes
      wrap-datasource
      wrap-config
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      logger/wrap-with-logger))
