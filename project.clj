(defproject tjape "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [duct/core "0.7.0"]
                 [duct/module.ataraxy "0.3.0"]
                 [duct/module.sql "0.5.0"]
                 [funcool/struct "1.3.0"]
                ]
  :repl-options {:init-ns tjape.core})
