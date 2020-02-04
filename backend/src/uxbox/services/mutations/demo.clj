;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2016-2020 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.services.mutations.demo
  "A demo specific mutations."
  (:require
   [clojure.spec.alpha :as s]
   [datoteka.core :as fs]
   [datoteka.storages :as ds]
   [promesa.core :as p]
   [promesa.exec :as px]
   [sodi.prng]
   [sodi.pwhash]
   [sodi.util]
   [uxbox.common.exceptions :as ex]
   [uxbox.common.spec :as us]
   [uxbox.config :as cfg]
   [uxbox.db :as db]
   [uxbox.emails :as emails]
   [uxbox.images :as images]
   [uxbox.media :as media]
   [uxbox.services.mutations :as sm]
   [uxbox.services.util :as su]
   [uxbox.services.mutations.profile :as profile]
   [uxbox.util.blob :as blob]
   [uxbox.util.uuid :as uuid]
   [vertx.core :as vc]))

(def sql:insert-user
  "insert into users (id, fullname, email, password, photo, is_demo)
   values ($1, $2, $3, $4, '', true) returning *")

(def sql:insert-email
  "insert into user_emails (user_id, email, is_main)
   values ($1, $2, true)")

(sm/defmutation ::create-demo-profile
  [params]
  (let [id (uuid/next)
        sem (System/currentTimeMillis)
        email    (str "demo-" sem ".demo@nodomain.com")
        fullname (str "Demo User " sem)
        password (-> (sodi.prng/random-bytes 12)
                     (sodi.util/bytes->b64s))
        password' (sodi.pwhash/derive password)]
    (db/with-atomic [conn db/pool]
      (db/query-one conn [sql:insert-user id fullname email password'])
      (db/query-one conn [sql:insert-email id email])
      {:email email
       :password password})))