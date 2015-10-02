(ns swagger-gen.language.scala-test
  (:require [clojure.test :refer :all]
            [swagger-gen.fixtures :refer :all]
            [swagger-gen.language.scala :refer :all]))

(def petstore-yaml "resources/swagger/petstore.yaml")

(deftest generate-case-object-test []
  (testing "should generate a Scala case object when arity is zero"
    (let [route {:name "Foo" :args []}]
      (is (= "case object Foo" (render-case-class route true))))))

(deftest enum-type-string-test []
  (testing "should generate enum string"
    (is (= "FooBarEnum.Type" (enum-type-string :foo :bar)))))

(deftest optional?-test []
  (testing "should convert to an option type"
    (is (= "Option[Foo] = None" (optional? false "Foo")))
    (is (= "Foo"                (optional? true  "Foo")))))

(deftest optional-enum-type-string-test []
  (testing "should generate optional enum string"
    (is (= "Option[FooBarEnum.Type] = None"
           (optional? false (enum-type-string :foo :bar))))))

(deftest enum-case-class-test []
  (testing "should render Scala enum types"
    (let [expected "case class Error(code: Int, message: ErrorMessageEnum.Type)"
          actual (render-case-class error-definition true)]
      (is (= expected actual)))))

(deftest optional-params-case-class-test []
  (testing "should handle optional params"
    (let [expected "case class Error(code: Option[Int] = None, message: Option[String] = None)"
          actual (render-case-class error-definition-with-optional-params true)]
      (is (= expected actual)))))