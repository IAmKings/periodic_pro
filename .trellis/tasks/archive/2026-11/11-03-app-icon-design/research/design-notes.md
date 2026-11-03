# Design exploration

## Category and references
Local logo library: education 23 marks, 43.5% blue; containment and negative space common.
Read geometry in pagekit.svg, framer.svg, kotlin-icon.svg and react.svg. Learn simple contours and clear counters; no tracing.
Atom subject lookup: React, Electron, Preact already occupy overlapping elliptical orbits. Avoid that motif.
Grid lookup: Microsoft four-square, Metabase dot-grid M. Avoid a plain four-square and dot letter.
Orbit lookup: Jupyter, Lua, Internet Explorer. Avoid slanted ellipse or an e with swoosh.

## Clichés to avoid
Three atomic ellipses, rainbow miniature periodic table, generic flask, tiny atomic mass/number, generic P in a stock font.

## Word map
Periodic → repetition, ordered steps, return; element → unit, cell, building block; Pro → precision, confident weight; learning → discovery, highlight, open boundary.
Intersections: order + cell; P + modular boundary; cycle + unit.

## Longlist (clarity / distinction / simplicity / relevance / small-size, 1–5)
1. Condense periodic table into a ten-cell stepped silhouette. Abstract / pictorial. 5/4/5/5/4 = 23. SELECT A.
2. Build a geometric P whose upper-right corner detaches as a new element. Letterform. 5/4/5/5/5 = 24. SELECT B.
3. Place a square element in an open circular cycle, with the next unit occupying the opening. Abstract. 4/4/5/4/5 = 22. SELECT C.
4. Full 18-column periodic silhouette with a highlighted cell. Pictorial. 5/2/2/5/1 = 15.
5. P cut into a chemical flask. Negative space. 3/2/3/4/3 = 15.
6. Three connected blocks stepping into an arrow. Abstract. 4/2/5/3/5 = 19.
7. Two bracket-like table halves create a central diamond. Negative space. 3/3/4/3/4 = 17.
8. A P shaped as one atomic orbit. Letterform. 4/2/4/4/4 = 18.
9. A nucleus formed from an open book. Pictorial. 3/2/3/4/2 = 14.
10. A pixelated infinity loop of repeating cells. Abstract. 3/2/3/4/2 = 14.

## Construction, v1
256 square canvas, black on transparent; key geometry on 8-unit grid.
A: 40-unit rounded cells, 16-unit gaps, four columns; two cells on first row, four each on second and third rows. Periodic-table silhouette, not a scientifically exact miniature table.
B: 32-unit P stem and bowl, 40-unit counter, rounded lower-right bowl. 32-unit detached top-right cell. Custom path, no font.
C: 32-unit open ring, square central element, a detached square in upper-right ring opening. Circular arcs and 90-degree forms.

## Constraints and follow-up
Concepts only. No trademark clearance. Android mask/safe-zone verification will happen on the selected direction before integration.

## Two refinement passes and review results
- v1: rendered review-v1.png and visually inspected all three at 64/32/16 px.
- v2: A corner radius 8 → 6 for crisper grid; B shifted +8/+8 for visual mass balance; C central cell 56 → 48 and shared radius 8 for more breathing room.
- Audited all v1 and v2 SVGs. No structural warnings/errors; only information about intentional unequal margins/optical placement. No live text, raster, filters, masks or strokes in symbols.
- Rendered tests.html at 1500×4400; tests-lower.html at 1500×3200 to inspect C and the reference shelf. Viewed both images. Inspected scale, white reversal, blur, mirror, 180° rotation and circle crop.
- At 16 px all silhouettes remain readable; the small separated cell in B softens, while the P remains clear. A is more generic and more densely detailed; C can evoke a progress indicator rather than chemistry.
- B recommended for continuity with existing P, reduced detail and direct association with Periodic / Pro / phosphorus. Colour hint uses existing #6650A4 and white.
- Final overview: concepts/concepts.png (1600×840); corresponding concepts.svg is a presentation image with live labels, not a logo master.
- No Android resources changed, build/tests not run because this is a design checkpoint. No trademark/reverse image clearance or physical launcher testing.
- Awaiting user direction before kit production, per logo-design Phase 6. Task remains planning; implementation has not started.
