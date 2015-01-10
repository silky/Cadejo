(ns sgwr.indicators.displaybar
  (:require [sgwr.cs.native :as native])
  (:require [sgwr.elements.drawing :as drawing])
  (:require [sgwr.indicators.char])
  (:require [sgwr.indicators.sixteen :as sixteen])
  (:require [sgwr.indicators.basic-char :as basic])
  (:require [sgwr.indicators.dot-matrix :as matrix])
  (:require [sgwr.util.utilities :as utilities]))

(def char-width sgwr.indicators.char/char-width)
(def char-height sgwr.indicators.char/char-height)
(def gap 5)
(def pad 4)

(defprotocol DisplayBar

  (widget-keys
    [this])
  
  (widget
    [this key])
  
  (colors! 
    [this inactive active])

  (character-count
    [this])

  (all-off!
    [this render?]
    [this])

  (display! 
    [this text render?]
    [this text]))

(defn- get-char-constructor [selector]
  (if (fn? selector) 
      selector
      (get {:basic basic/basic-char
            :sixteen sixteen/char-16
            16 sixteen/char-16
            :matrix matrix/matrix-char}
           selector
           basic/basic-char)))

(defn displaybar 

  ([char-count]
   (displaybar char-count :matrix))

  ([char-count ctype] 
   (let [w (+ (* 2 pad)(* char-count (+ char-width gap)))
         h (+ (* 2 pad) char-height)
         x-offset (if (= ctype :matrix) (* 2 pad) pad)
         y-offset pad
         drw (drawing/native-drawing w h)
         grp (.root drw)]
     (displaybar grp x-offset y-offset char-count ctype)))

  ;; ctype - one of :basic, 16 or :sixteen, :matrix
  ([grp x-offset y-offset char-count ctype]
   (let [elements (let [chr-fn (get-char-constructor ctype)
                        acc* (atom [])
                        w (+ char-width gap)]
                    (dotimes [i char-count]
                      (let [x (+ x-offset (* i w))
                            y y-offset
                            cobj (chr-fn grp x y)]
                        (swap! acc* (fn [q](conj q cobj)))))
                    @acc*)
         render-drawing (fn [flag]
                          (let [drw (.get-property grp :drawing)]
                            (if (and drw flag)
                              (.render drw))))
         obj (reify DisplayBar
               
               (widget-keys [this]
                 [:group :drawing])

               (widget [this key]
                 (cond (= key :group) grp
                       (= key :drawing)(.get-property grp :drawing)
                       :default
                       (utilities/warning (format "DisplayBar does not have %s widget" key))))
                 

               (colors! [this inactive active]
                 (doseq [e elements]
                   (.colors! e inactive active)))
               
               (character-count [this]
                 (count elements))
               
               (all-off! [this render?]
                 (doseq [e elements]
                   (.display! e \space))
                 (render-drawing render?))
               
               (all-off! [this]
                 (.all-off! this true))
               
               (display! [this text render?]
                 (.all-off! this false)
                 (let [i* (atom 0)
                       limit (min (count text)(.character-count this))]
                   (while (< @i* limit)
                     (.display! (nth elements @i*)(nth text @i*))
                     (swap! i* inc))
                   (render-drawing render?)))
               
               (display! [this text]
                 (.display! this text true)) )]
     obj)))