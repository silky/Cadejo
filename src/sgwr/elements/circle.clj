;; TODO:
;;     1. Define two-point (diameter) constructor
;;     2. Define center, radius constructor
;;     3. define three-point constructor

(ns sgwr.elements.circle
  (:require [sgwr.util.math :as math])
  (:require [sgwr.elements.element])
  (:require [seesaw.graphics :as ssg]))

(defn- shape-fn [obj]
  (let [cs (.coordinate-system obj)
        [p0 p1](.points obj)
        q0 (.map-point cs p0)
        q1 (.map-point cs p1)
        [u0 v0] q0
        [u1 v1] q1
        w (- u1 u0)
        h (- v1 v0)]
    (ssg/ellipse u0 v0 w h)))

(defn- update-fn [obj points]   ;; points [p0 p1] -> bounding rectangle
  (let [[p0 p1] points
        [x0 y0] p0
        [x1 y1] p1
        xc (math/mean x0 x1)
        yc (math/mean y0 y1)
        dx (math/abs (- x1 x0))
        radius (* 0.5 dx)]
    (.put-property! obj :center [xc yc])
    (.put-property! obj :radius radius)
    points))

(defn- distance-helper [obj q]
  (let [pc (.get-property! obj :center)
        radius (.get-property! obj :radius)
        dc (math/distance pc q)
        distance (- dc radius)]
    [(<= dc radius)
     (max 0 distance)]))

(defn contains-fn [obj q]
  (first (distance-helper obj q)))

(defn distance-fn [obj q]
  (second (distance-helper q)))

(defn- bounds-fn [obj points]
  (let [x (map first points)
        y (map second points)
        x0 (apply min x)
        x1 (apply max x)
        y0 (apply min y)
        y1 (apply max y)]
    [[x0 y0][x1 y1]])) 

(def ^:private circle-function-map {:shape-fn shape-fn
                                    :contains-fn contains-fn
                                    :distance-fn distance-fn
                                    :update-fn update-fn
                                    :bounds-fn bounds-fn})

(def locked-properties [:id :center :radius])

; circle defined by bounding rectangle
; If rectangle is not square, the side with greatest length is used

(defn circle
  "(circle)
   (circle parent p0 p1)
   
   Create circle with bounding square p0 p1
   If rectangle [p0 p1] is not square use a square with sides equal to
   the longest side of [p0 p1].
 
   Circle objects 'containe' a point q if the distance between q and
   the center point is less the or equal to the radius. 

   The distance between all points enclosed by the locus of the
   circle is defined as 0.  For points q outside the circle's locus the
   distance is defined as the distance between q and the point of
   intersection between the circle and a line through q and normal to the
   circle."  


  ([](circle nil [-1 -1][1 1]))         
  ([parent p0 p1]
   (let [x0 (apply min (map first [p0 p1]))
         y0 (apply min (map second [p0 p1]))
         x1 (apply max (map first [p0 p1]))
         y1 (apply max (map second [p0 p1]))
         side (max (- x1 x0)(- y1 y0))
         obj (sgwr.elements.element/create-element :circle
                                                   parent
                                                   circle-function-map
                                                   locked-properties)]
     (.set-points! obj [[x0 y0][(+ x0 side)(+ y0 side)]])
     (.put-property! obj :id :circle)
     (if parent (.set-parent! obj parent))
     obj)))