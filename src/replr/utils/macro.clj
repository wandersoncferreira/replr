(ns replr.utils.macro)

(defmacro if-lety
  "The same as `if-let` but tests if the list is empty."
  ([bindings then]
   `(if-lety ~bindings ~then nil))
  ([bindings then else & _]
   (let [form (bindings 0) tst (bindings 1)]
     `(let [temp# ~tst]
        (if (and temp# (not-empty temp#))
          (let [~form temp#]
            ~then)
          ~else)))))
