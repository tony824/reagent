(ns reagenttest.runtests
  (:require [reagenttest.testreagent]
            [reagenttest.testcursor]
            [reagenttest.testinterop]
            [reagenttest.testratom]
            [reagenttest.testratomasync]
            [reagenttest.testtrack]
            [reagenttest.testwithlet]
            [reagenttest.testwrap]
            [cljs.test :as test :include-macros true]
            [doo.runner :as doo :refer-macros [doo-tests]]
            [reagent.core :as r]
            [reagent.debug :refer-macros [dbg log]]
            [sitetools.server]))

(enable-console-print!)

(def test-results (r/atom nil))

(def test-box-style {:position 'absolute
                     :margin-left -35
                     :color :#aaa})

(defn all-tests []
  #_(test/run-tests 'reagenttest.testratomasync)
  (test/run-all-tests #"reagenttest.test.*"))

(defmethod test/report [::test/default :summary] [m]
  ;; ClojureScript 2814 doesn't return anything from run-tests
  (reset! test-results m)
  (println "\nRan" (:test m) "tests containing"
    (+ (:pass m) (:fail m) (:error m)) "assertions.")
  (println (:fail m) "failures," (:error m) "errors."))

(defn run-tests []
  (reset! test-results nil)
  (if r/is-client
    (js/setTimeout all-tests 100)
    (all-tests)))

(defn test-output-mini []
  (let [res @test-results]
    [:div {:style test-box-style
           :on-click run-tests}
     (if res
       (if (zero? (+ (:fail res) (:error res)))
         "All tests ok"
         [:span "Test failure: "
          (:fail res) " failures, " (:error res) " errors."])
       "testing")]))

(defn init! []
  ;; This function is only used when running tests from the demo app.
  ;; Which is why exit-point is set manually.
  (when (some? (test/deftest empty-test))
    (doo/set-exit-point! (fn [success?] nil))
    (run-tests)
    [#'test-output-mini]))

(doo-tests 'reagenttest.testreagent
           'reagenttest.testcursor
           'reagenttest.testinterop
           'reagenttest.testratom
           'reagenttest.testratomasync
           'reagenttest.testtrack
           'reagenttest.testwithlet
           'reagenttest.testwrap)
