(ns swagger-gen.core
  (:require [clojure.walk :refer [keywordize-keys]]
            [cheshire.core :as json]
            [swagger-gen.util :refer [normalize-def]]
            [yaml.core :as yml]))

(defn yaml-path? [path-to-file]
  (or (.endsWith path-to-file ".yaml")
      (.endsWith path-to-file ".yml")))

(defn load-swagger
  "Load a swagger spec from disk.
   File can be either yaml or json format"
  [path-to-file]
  (if (yaml-path? path-to-file)
    (yml/from-file path-to-file false)
    (->> path-to-file slurp json/parse-string)))

(defn- get-section
  "Util function for extracting swagger spec sections
    e.g (get-section :paths)"
  [spec section]
  (get spec (name section)))

(defn normalize-definition
  "Take a raw YAML definition from swagger and normalize it
   to a form that is easier to work with and parse"
  [definition]
  (let [properties (->> definition :attributes :properties)
        required-attributes
          (set (map keyword
                 (->> definition :attributes :required)))]
    {:name (:name definition)
     :args (into []
             (for [[property attrs] properties]
               {:name (name property)
                :type (normalize-def (or (:type attrs) (:$ref attrs)))
                :items (or (vals (:items attrs)) [])
                :required (contains? required-attributes property)}))}))

(defn get-definitions [spec]
  (when-let [data (keywordize-keys (get-section spec :definitions))]
    (map (fn [definition]
           (let [[class-name attributes] definition]
             {:name (name class-name)
              :attributes attributes})) data)))

(defn swagger-defs
  "Extract all swagger definitions/models from a spec"
  [spec]
  (when-let [definitions (get-definitions spec)]
    (map normalize-definition definitions)))

(defn params-of-type 
  [swagger-route param-type]
  (->> swagger-route 
       :parameters 
       (filter #(= (:in %) param-type))
       (into [])))

(defn query-params [swagger-route]
  (params-of-type swagger-route "query"))

(defn swagger-paths
  "Extract all HTTP request paths from a swagger spec"
  [spec]
  (when-let [data (get-section spec :paths)]
    (into []
      (flatten
        (for [[k v] data]
          (for [[method attributes] v]
            (merge {:method method :path k}
                   (keywordize-keys attributes))))))))

(defn swagger-info [spec]
  (get-section spec :info))

(defn parse-swagger
  "Load a swagger specification from file path and convert it into
   a sane/traversable format making it easier to work with"
  [path-to-swagger]
  (let [data (load-swagger path-to-swagger)]
    {:info (swagger-info data)
     :paths (swagger-paths data)
     :definitions (swagger-defs data)}))
