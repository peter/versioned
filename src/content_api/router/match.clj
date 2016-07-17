(ns content-api.router.match
  (:require [content-api.router.params :as p]))

(defn method-match
  "If HTTP request method matches route return true otherwise false"
  [route request]
  ((:methods route) (:request-method request)))

(defn uri-match
  "if uri matches return route params map, else return nil"
  [route request]
  (let [path (:path route)
        uri (:uri request)
        names (p/param-names path)]
    (if (empty? names)
      (if (= path uri) {} nil)
      (p/params names path uri))))

(defn match [route request]
  (and
    (method-match route request)
    (uri-match route request)))

(defn find-match [routes request]
  (let [matcher (fn [route]
                   (let [params (match route request)]
                     (when params {:route route :params params})))]
    (some matcher routes)))
