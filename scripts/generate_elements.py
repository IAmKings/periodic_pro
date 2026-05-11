#!/usr/bin/env python3
"""Generate elements.json and elements_zh.json for Periodic Pro."""

import json

# ============================================================
# 118 elements data
# Fields: atomicNumber, symbol, name, atomicMass, category,
#         electronConfiguration, electronegativity, atomicRadius,
#         ionizationEnergy, density, meltingPoint, boilingPoint,
#         group, period, discoveredBy, yearDiscovered
# ============================================================

# Category mapping from Bowserinator categories to our 10 categories
def map_category(bowser_cat):
    mapping = {
        "diatomic nonmetal": "nonmetal",
        "polyatomic nonmetal": "nonmetal",
        "noble gas": "noble-gas",
        "alkali metal": "alkali-metal",
        "alkaline earth metal": "alkaline-earth-metal",
        "transition metal": "transition-metal",
        "post-transition metal": "post-transition-metal",
        "metalloid": "metalloid",
        "lanthanide": "lanthanide",
        "actinide": "actinide",
        "halogen": "halogen",
    }
    return mapping.get(bowser_cat, "unknown")

elements_data = [
    # (z, symbol, name, mass, category_bowser, e_config, en, radius, ie, density, melt, boil, group, period, discoverer, year)
    (1, "H", "Hydrogen", 1.008, "diatomic nonmetal", "1s1", 2.2, 120, 13.598, 0.00008988, 13.99, 20.271, 1, 1, "Henry Cavendish", 1766),
    (2, "He", "Helium", 4.0026, "noble gas", "1s2", None, 140, 24.587, 0.0001785, 0.95, 4.222, 18, 1, "Pierre Janssen", 1868),
    (3, "Li", "Lithium", 6.94, "alkali metal", "1s2 2s1", 0.98, 182, 5.392, 0.534, 453.65, 1603, 1, 2, "Johan August Arfwedson", 1817),
    (4, "Be", "Beryllium", 9.0122, "alkaline earth metal", "1s2 2s2", 1.57, 153, 9.323, 1.85, 1560, 2742, 2, 2, "Louis Nicolas Vauquelin", 1798),
    (5, "B", "Boron", 10.81, "metalloid", "1s2 2s2 2p1", 2.04, 192, 8.298, 2.34, 2349, 4200, 13, 2, "Joseph Louis Gay-Lussac", 1808),
    (6, "C", "Carbon", 12.011, "polyatomic nonmetal", "1s2 2s2 2p2", 2.55, 170, 11.260, 2.267, 3823, 4098, 14, 2, "Ancient Egypt", None),
    (7, "N", "Nitrogen", 14.007, "diatomic nonmetal", "1s2 2s2 2p3", 3.04, 155, 14.534, 0.0012506, 63.15, 77.355, 15, 2, "Daniel Rutherford", 1772),
    (8, "O", "Oxygen", 15.999, "diatomic nonmetal", "1s2 2s2 2p4", 3.44, 152, 13.618, 0.001429, 54.36, 90.188, 16, 2, "Joseph Priestley", 1774),
    (9, "F", "Fluorine", 18.998, "diatomic nonmetal", "1s2 2s2 2p5", 3.98, 135, 17.423, 0.001696, 53.53, 85.03, 17, 2, "André-Marie Ampère", 1886),
    (10, "Ne", "Neon", 20.180, "noble gas", "1s2 2s2 2p6", None, 154, 21.565, 0.0008999, 24.56, 27.07, 18, 2, "William Ramsay", 1898),
    (11, "Na", "Sodium", 22.990, "alkali metal", "[Ne] 3s1", 0.93, 227, 5.139, 0.968, 370.87, 1156, 1, 3, "Humphry Davy", 1807),
    (12, "Mg", "Magnesium", 24.305, "alkaline earth metal", "[Ne] 3s2", 1.31, 173, 7.646, 1.738, 923, 1363, 2, 3, "Joseph Black", 1755),
    (13, "Al", "Aluminium", 26.982, "post-transition metal", "[Ne] 3s2 3p1", 1.61, 184, 5.986, 2.698, 933.47, 2792, 13, 3, "Hans Christian Ørsted", 1825),
    (14, "Si", "Silicon", 28.085, "metalloid", "[Ne] 3s2 3p2", 1.9, 210, 8.152, 2.329, 1687, 3538, 14, 3, "Jöns Jacob Berzelius", 1824),
    (15, "P", "Phosphorus", 30.974, "polyatomic nonmetal", "[Ne] 3s2 3p3", 2.19, 180, 10.487, 1.823, 317.3, 553.6, 15, 3, "Hennig Brand", 1669),
    (16, "S", "Sulfur", 32.06, "polyatomic nonmetal", "[Ne] 3s2 3p4", 2.58, 180, 10.360, 2.067, 388.36, 717.8, 16, 3, "Ancient China", None),
    (17, "Cl", "Chlorine", 35.45, "halogen", "[Ne] 3s2 3p5", 3.16, 175, 12.968, 0.003214, 171.6, 239.11, 17, 3, "Carl Wilhelm Scheele", 1774),
    (18, "Ar", "Argon", 39.948, "noble gas", "[Ne] 3s2 3p6", None, 188, 15.760, 0.0017837, 83.8, 87.3, 18, 3, "William Ramsay", 1894),
    (19, "K", "Potassium", 39.098, "alkali metal", "[Ar] 4s1", 0.82, 275, 4.341, 0.862, 336.53, 1032, 1, 4, "Humphry Davy", 1807),
    (20, "Ca", "Calcium", 40.078, "alkaline earth metal", "[Ar] 4s2", 1.0, 231, 6.113, 1.55, 1115, 1757, 2, 4, "Humphry Davy", 1808),
    (21, "Sc", "Scandium", 44.956, "transition metal", "[Ar] 3d1 4s2", 1.36, 211, 6.561, 2.989, 1814, 3109, 3, 4, "Lars Fredrik Nilson", 1879),
    (22, "Ti", "Titanium", 47.867, "transition metal", "[Ar] 3d2 4s2", 1.54, 200, 6.828, 4.506, 1941, 3560, 4, 4, "William Gregor", 1791),
    (23, "V", "Vanadium", 50.942, "transition metal", "[Ar] 3d3 4s2", 1.63, 192, 6.746, 6.11, 2183, 3680, 5, 4, "Andrés Manuel del Río", 1801),
    (24, "Cr", "Chromium", 51.996, "transition metal", "[Ar] 3d5 4s1", 1.66, 200, 6.767, 7.15, 2180, 2944, 6, 4, "Louis Nicolas Vauquelin", 1797),
    (25, "Mn", "Manganese", 54.938, "transition metal", "[Ar] 3d5 4s2", 1.55, 210, 7.434, 7.21, 1519, 2334, 7, 4, "Carl Wilhelm Scheele", 1774),
    (26, "Fe", "Iron", 55.845, "transition metal", "[Ar] 3d6 4s2", 1.83, 200, 7.902, 7.874, 1811, 3134, 8, 4, "Ancient", None),
    (27, "Co", "Cobalt", 58.933, "transition metal", "[Ar] 3d7 4s2", 1.88, 200, 7.881, 8.86, 1768, 3200, 9, 4, "Georg Brandt", 1735),
    (28, "Ni", "Nickel", 58.693, "transition metal", "[Ar] 3d8 4s2", 1.91, 200, 7.640, 8.912, 1728, 3186, 10, 4, "Axel Fredrik Cronstedt", 1751),
    (29, "Cu", "Copper", 63.546, "transition metal", "[Ar] 3d10 4s1", 1.9, 200, 7.726, 8.96, 1357.77, 2835, 11, 4, "Ancient", None),
    (30, "Zn", "Zinc", 65.38, "transition metal", "[Ar] 3d10 4s2", 1.65, 210, 9.394, 7.134, 692.68, 1180, 12, 4, "Ancient India", None),
    (31, "Ga", "Gallium", 69.723, "post-transition metal", "[Ar] 3d10 4s2 4p1", 1.81, 180, 5.999, 5.904, 302.91, 2477, 13, 4, "Lecoq de Boisbaudran", 1875),
    (32, "Ge", "Germanium", 72.63, "metalloid", "[Ar] 3d10 4s2 4p2", 2.01, 210, 7.899, 5.323, 1211.4, 3106, 14, 4, "Clemens Winkler", 1886),
    (33, "As", "Arsenic", 74.922, "metalloid", "[Ar] 3d10 4s2 4p3", 2.18, 185, 9.789, 5.776, 1090, 887, 15, 4, "Albertus Magnus", 1250),
    (34, "Se", "Selenium", 78.971, "polyatomic nonmetal", "[Ar] 3d10 4s2 4p4", 2.55, 190, 9.752, 4.809, 493, 958, 16, 4, "Jöns Jacob Berzelius", 1817),
    (35, "Br", "Bromine", 79.904, "halogen", "[Ar] 3d10 4s2 4p5", 2.96, 185, 11.814, 3.122, 265.8, 332.0, 17, 4, "Antoine Jérôme Balard", 1826),
    (36, "Kr", "Krypton", 83.798, "noble gas", "[Ar] 3d10 4s2 4p6", 3.0, 202, 13.999, 0.003733, 115.78, 119.93, 18, 4, "William Ramsay", 1898),
    (37, "Rb", "Rubidium", 85.468, "alkali metal", "[Kr] 5s1", 0.82, 303, 4.177, 1.532, 312.45, 961, 1, 5, "Robert Bunsen", 1861),
    (38, "Sr", "Strontium", 87.62, "alkaline earth metal", "[Kr] 5s2", 0.95, 249, 5.695, 2.64, 1050, 1655, 2, 5, "William Cruickshank", 1790),
    (39, "Y", "Yttrium", 88.906, "transition metal", "[Kr] 4d1 5s2", 1.22, 230, 6.217, 4.472, 1799, 3609, 3, 5, "Johan Gadolin", 1794),
    (40, "Zr", "Zirconium", 91.224, "transition metal", "[Kr] 4d2 5s2", 1.33, 230, 6.634, 6.506, 2128, 4682, 4, 5, "Martin Heinrich Klaproth", 1789),
    (41, "Nb", "Niobium", 92.906, "transition metal", "[Kr] 4d4 5s1", 1.6, 215, 6.759, 8.57, 2750, 5017, 5, 5, "Charles Hatchett", 1801),
    (42, "Mo", "Molybdenum", 95.95, "transition metal", "[Kr] 4d5 5s1", 2.16, 210, 7.092, 10.22, 2896, 4912, 6, 5, "Carl Wilhelm Scheele", 1778),
    (43, "Tc", "Technetium", 98.0, "transition metal", "[Kr] 4d5 5s2", 1.9, 210, 7.28, 11.5, 2430, 4538, 7, 5, "Emilio Segrè", 1937),
    (44, "Ru", "Ruthenium", 101.07, "transition metal", "[Kr] 4d7 5s1", 2.2, 220, 7.361, 12.37, 2607, 4423, 8, 5, "Karl Ernst Claus", 1844),
    (45, "Rh", "Rhodium", 102.91, "transition metal", "[Kr] 4d8 5s1", 2.28, 210, 7.459, 12.41, 2237, 3968, 9, 5, "William Hyde Wollaston", 1803),
    (46, "Pd", "Palladium", 106.42, "transition metal", "[Kr] 4d10", 2.2, 210, 8.337, 12.02, 1828.05, 3236, 10, 5, "William Hyde Wollaston", 1803),
    (47, "Ag", "Silver", 107.87, "transition metal", "[Kr] 4d10 5s1", 1.93, 210, 7.576, 10.501, 1234.93, 2435, 11, 5, "Ancient", None),
    (48, "Cd", "Cadmium", 112.41, "transition metal", "[Kr] 4d10 5s2", 1.69, 220, 8.994, 8.69, 594.22, 1040, 12, 5, "Friedrich Stromeyer", 1817),
    (49, "In", "Indium", 114.82, "post-transition metal", "[Kr] 4d10 5s2 5p1", 1.78, 200, 5.786, 7.31, 429.75, 2345, 13, 5, "Ferdinand Reich", 1863),
    (50, "Sn", "Tin", 118.71, "post-transition metal", "[Kr] 4d10 5s2 5p2", 1.96, 225, 7.344, 7.287, 505.08, 2875, 14, 5, "Ancient", None),
    (51, "Sb", "Antimony", 121.76, "metalloid", "[Kr] 4d10 5s2 5p3", 2.05, 200, 8.608, 6.685, 903.78, 1860, 15, 5, "Ancient", None),
    (52, "Te", "Tellurium", 127.6, "metalloid", "[Kr] 4d10 5s2 5p4", 2.1, 210, 9.010, 6.232, 722.66, 1261, 16, 5, "Franz-Joseph Müller von Reichenstein", 1782),
    (53, "I", "Iodine", 126.90, "halogen", "[Kr] 4d10 5s2 5p5", 2.66, 215, 10.451, 4.93, 386.85, 457.4, 17, 5, "Bernard Courtois", 1811),
    (54, "Xe", "Xenon", 131.29, "noble gas", "[Kr] 4d10 5s2 5p6", 2.6, 216, 12.130, 0.005887, 161.4, 165.03, 18, 5, "William Ramsay", 1898),
    (55, "Cs", "Caesium", 132.91, "alkali metal", "[Xe] 6s1", 0.79, 343, 3.894, 1.873, 301.59, 944, 1, 6, "Robert Bunsen", 1860),
    (56, "Ba", "Barium", 137.33, "alkaline earth metal", "[Xe] 6s2", 0.89, 268, 5.212, 3.594, 1000, 2118, 2, 6, "Carl Wilhelm Scheele", 1772),
    (57, "La", "Lanthanum", 138.91, "lanthanide", "[Xe] 5d1 6s2", 1.1, 240, 5.577, 6.145, 1193, 3737, 3, 6, "Carl Gustaf Mosander", 1838),
    (58, "Ce", "Cerium", 140.12, "lanthanide", "[Xe] 4f1 5d1 6s2", 1.12, 235, 5.539, 6.77, 1068, 3716, 3, 6, "Martin Heinrich Klaproth", 1803),
    (59, "Pr", "Praseodymium", 140.91, "lanthanide", "[Xe] 4f3 6s2", 1.13, 239, 5.464, 6.773, 1208, 3793, 3, 6, "Carl Gustaf Mosander", 1841),
    (60, "Nd", "Neodymium", 144.24, "lanthanide", "[Xe] 4f4 6s2", 1.14, 229, 5.525, 7.007, 1297, 3347, 3, 6, "Carl Gustaf Mosander", 1841),
    (61, "Pm", "Promethium", 145.0, "lanthanide", "[Xe] 4f5 6s2", 1.13, 236, 5.582, 7.26, 1315, 3273, 3, 6, "Jacob A. Marinsky", 1945),
    (62, "Sm", "Samarium", 150.36, "lanthanide", "[Xe] 4f6 6s2", 1.17, 229, 5.643, 7.52, 1345, 2067, 3, 6, "Lecoq de Boisbaudran", 1879),
    (63, "Eu", "Europium", 151.96, "lanthanide", "[Xe] 4f7 6s2", 1.2, 233, 5.670, 5.243, 1099, 1802, 3, 6, "Eugène-Anatole Demarçay", 1896),
    (64, "Gd", "Gadolinium", 157.25, "lanthanide", "[Xe] 4f7 5d1 6s2", 1.2, 237, 6.149, 7.9, 1585, 3546, 3, 6, "Jean Charles Galissard de Marignac", 1880),
    (65, "Tb", "Terbium", 158.93, "lanthanide", "[Xe] 4f9 6s2", 1.2, 221, 5.864, 8.229, 1629, 3503, 3, 6, "Carl Gustaf Mosander", 1843),
    (66, "Dy", "Dysprosium", 162.5, "lanthanide", "[Xe] 4f10 6s2", 1.22, 229, 5.939, 8.55, 1680, 2840, 3, 6, "Lecoq de Boisbaudran", 1886),
    (67, "Ho", "Holmium", 164.93, "lanthanide", "[Xe] 4f11 6s2", 1.23, 216, 6.022, 8.795, 1734, 2993, 3, 6, "Marc Delafontaine", 1878),
    (68, "Er", "Erbium", 167.26, "lanthanide", "[Xe] 4f12 6s2", 1.24, 235, 6.108, 9.066, 1802, 3141, 3, 6, "Carl Gustaf Mosander", 1843),
    (69, "Tm", "Thulium", 168.93, "lanthanide", "[Xe] 4f13 6s2", 1.25, 227, 6.184, 9.321, 1818, 2223, 3, 6, "Per Teodor Cleve", 1879),
    (70, "Yb", "Ytterbium", 173.04, "lanthanide", "[Xe] 4f14 6s2", 1.1, 242, 6.254, 6.965, 1097, 1469, 3, 6, "Jean Charles Galissard de Marignac", 1878),
    (71, "Lu", "Lutetium", 174.97, "lanthanide", "[Xe] 4f14 5d1 6s2", 1.27, 221, 6.289, 9.84, 1925, 3675, 3, 6, "Georges Urbain", 1906),
    (72, "Hf", "Hafnium", 178.49, "transition metal", "[Xe] 4f14 5d2 6s2", 1.3, 225, 6.825, 13.31, 2506, 4876, 4, 6, "Dirk Coster", 1923),
    (73, "Ta", "Tantalum", 180.95, "transition metal", "[Xe] 4f14 5d3 6s2", 1.5, 220, 7.549, 16.69, 3290, 5731, 5, 6, "Anders Gustaf Ekeberg", 1802),
    (74, "W", "Tungsten", 183.84, "transition metal", "[Xe] 4f14 5d4 6s2", 2.36, 210, 7.864, 19.25, 3695, 5828, 6, 6, "Carl Wilhelm Scheele", 1781),
    (75, "Re", "Rhenium", 186.21, "transition metal", "[Xe] 4f14 5d5 6s2", 1.9, 215, 7.833, 21.02, 3459, 5869, 7, 6, "Masataka Ogawa", 1908),
    (76, "Os", "Osmium", 190.23, "transition metal", "[Xe] 4f14 5d6 6s2", 2.2, 210, 8.438, 22.59, 3306, 5285, 8, 6, "Smithson Tennant", 1803),
    (77, "Ir", "Iridium", 192.22, "transition metal", "[Xe] 4f14 5d7 6s2", 2.2, 210, 8.967, 22.56, 2719, 4701, 9, 6, "Smithson Tennant", 1803),
    (78, "Pt", "Platinum", 195.08, "transition metal", "[Xe] 4f14 5d9 6s1", 2.28, 210, 8.959, 21.46, 2041.4, 4098, 10, 6, "Ancient", None),
    (79, "Au", "Gold", 196.97, "transition metal", "[Xe] 4f14 5d10 6s1", 2.54, 210, 9.226, 19.32, 1337.33, 3129, 11, 6, "Ancient", None),
    (80, "Hg", "Mercury", 200.59, "post-transition metal", "[Xe] 4f14 5d10 6s2", 2.0, 210, 10.438, 13.5336, 234.32, 629.88, 12, 6, "Ancient", None),
    (81, "Tl", "Thallium", 204.38, "post-transition metal", "[Xe] 4f14 5d10 6s2 6p1", 1.62, 210, 6.108, 11.85, 577, 1746, 13, 6, "William Crookes", 1861),
    (82, "Pb", "Lead", 207.2, "post-transition metal", "[Xe] 4f14 5d10 6s2 6p2", 2.33, 210, 7.417, 11.34, 600.61, 2022, 14, 6, "Ancient", None),
    (83, "Bi", "Bismuth", 208.98, "post-transition metal", "[Xe] 4f14 5d10 6s2 6p3", 2.02, 210, 7.286, 9.78, 544.7, 1837, 15, 6, "Ancient", None),
    (84, "Po", "Polonium", 209.0, "post-transition metal", "[Xe] 4f14 5d10 6s2 6p4", 2.0, 190, 8.417, 9.196, 527, 1235, 16, 6, "Marie Curie", 1898),
    (85, "At", "Astatine", 210.0, "halogen", "[Xe] 4f14 5d10 6s2 6p5", 2.2, 200, 9.318, 6.35, 575, 610, 17, 6, "Dale R. Corson", 1940),
    (86, "Rn", "Radon", 222.0, "noble gas", "[Xe] 4f14 5d10 6s2 6p6", 2.2, 220, 10.748, 0.00973, 202, 211.5, 18, 6, "Friedrich Ernst Dorn", 1900),
    (87, "Fr", "Francium", 223.0, "alkali metal", "[Rn] 7s1", 0.79, 348, 4.073, 1.87, 300, 950, 1, 7, "Marguerite Perey", 1939),
    (88, "Ra", "Radium", 226.0, "alkaline earth metal", "[Rn] 7s2", 0.9, 283, 5.279, 5.5, 973, 1413, 2, 7, "Marie Curie", 1898),
    (89, "Ac", "Actinium", 227.0, "actinide", "[Rn] 6d1 7s2", 1.1, 260, 5.380, 10.07, 1323, 3471, 3, 7, "Friedrich Oskar Giesel", 1902),
    (90, "Th", "Thorium", 232.04, "actinide", "[Rn] 6d2 7s2", 1.3, 240, 6.307, 11.72, 2115, 5061, 3, 7, "Jöns Jacob Berzelius", 1828),
    (91, "Pa", "Protactinium", 231.04, "actinide", "[Rn] 5f2 6d1 7s2", 1.5, 230, 5.890, 15.37, 1841, 4300, 3, 7, "Otto Hahn", 1913),
    (92, "U", "Uranium", 238.03, "actinide", "[Rn] 5f3 6d1 7s2", 1.38, 230, 6.194, 18.95, 1405.3, 4404, 3, 7, "Martin Heinrich Klaproth", 1789),
    (93, "Np", "Neptunium", 237.0, "actinide", "[Rn] 5f4 6d1 7s2", 1.36, 230, 6.266, 20.25, 912, 4273, 3, 7, "Edwin McMillan", 1940),
    (94, "Pu", "Plutonium", 244.0, "actinide", "[Rn] 5f6 7s2", 1.28, 230, 6.026, 19.84, 912.5, 3501, 3, 7, "Glenn T. Seaborg", 1940),
    (95, "Am", "Americium", 243.0, "actinide", "[Rn] 5f7 7s2", 1.3, 230, 5.974, 13.69, 1449, 2880, 3, 7, "Glenn T. Seaborg", 1944),
    (96, "Cm", "Curium", 247.0, "actinide", "[Rn] 5f7 6d1 7s2", 1.3, 230, 5.992, 13.51, 1613, 3383, 3, 7, "Glenn T. Seaborg", 1944),
    (97, "Bk", "Berkelium", 247.0, "actinide", "[Rn] 5f9 7s2", 1.3, 230, 6.198, 13.25, 1259, 2900, 3, 7, "Stanley G. Thompson", 1949),
    (98, "Cf", "Californium", 251.0, "actinide", "[Rn] 5f10 7s2", 1.3, 230, 6.282, 15.1, 1173, 1743, 3, 7, "Stanley G. Thompson", 1950),
    (99, "Es", "Einsteinium", 252.0, "actinide", "[Rn] 5f11 7s2", 1.3, 230, 6.422, 13.5, 1133, 1269, 3, 7, "Albert Ghiorso", 1952),
    (100, "Fm", "Fermium", 257.0, "actinide", "[Rn] 5f12 7s2", 1.3, 230, 6.500, None, 1125, None, 3, 7, "Albert Ghiorso", 1952),
    (101, "Md", "Mendelevium", 258.0, "actinide", "[Rn] 5f13 7s2", 1.3, 230, 6.580, None, 1100, None, 3, 7, "Albert Ghiorso", 1955),
    (102, "No", "Nobelium", 259.0, "actinide", "[Rn] 5f14 7s2", 1.3, 230, 6.650, None, 1100, None, 3, 7, "Albert Ghiorso", 1958),
    (103, "Lr", "Lawrencium", 266.0, "actinide", "[Rn] 5f14 7s2 7p1", 1.3, 230, 4.900, None, 1900, None, 3, 7, "Albert Ghiorso", 1961),
    # Superheavy elements (Z=104-118) - many fields are null
    (104, "Rf", "Rutherfordium", 267.0, "transition metal", "[Rn] 5f14 6d2 7s2", None, None, 6.000, None, 2400, None, 4, 7, "Albert Ghiorso", 1964),
    (105, "Db", "Dubnium", 268.0, "transition metal", "[Rn] 5f14 6d3 7s2", None, None, None, None, None, None, 5, 7, "Albert Ghiorso", 1967),
    (106, "Sg", "Seaborgium", 269.0, "transition metal", "[Rn] 5f14 6d4 7s2", None, None, None, None, None, None, 6, 7, "Albert Ghiorso", 1974),
    (107, "Bh", "Bohrium", 270.0, "transition metal", "[Rn] 5f14 6d5 7s2", None, None, None, None, None, None, 7, 7, "Peter Armbruster", 1981),
    (108, "Hs", "Hassium", 277.0, "transition metal", "[Rn] 5f14 6d6 7s2", None, None, None, None, None, None, 8, 7, "Peter Armbruster", 1984),
    (109, "Mt", "Meitnerium", 278.0, "transition metal", "[Rn] 5f14 6d7 7s2", None, None, None, None, None, None, 9, 7, "Peter Armbruster", 1982),
    (110, "Ds", "Darmstadtium", 281.0, "transition metal", "[Rn] 5f14 6d8 7s2", None, None, None, None, None, None, 10, 7, "Sigurd Hofmann", 1994),
    (111, "Rg", "Roentgenium", 282.0, "transition metal", "[Rn] 5f14 6d9 7s2", None, None, None, None, None, None, 11, 7, "Sigurd Hofmann", 1994),
    (112, "Cn", "Copernicium", 285.0, "transition metal", "[Rn] 5f14 6d10 7s2", None, None, None, None, None, None, 12, 7, "Sigurd Hofmann", 1996),
    (113, "Nh", "Nihonium", 286.0, "post-transition metal", "[Rn] 5f14 6d10 7s2 7p1", None, None, None, None, None, None, 13, 7, "Kōsuke Morita", 2004),
    (114, "Fl", "Flerovium", 289.0, "post-transition metal", "[Rn] 5f14 6d10 7s2 7p2", None, None, None, None, None, None, 14, 7, "Yuri Oganessian", 1999),
    (115, "Mc", "Moscovium", 290.0, "post-transition metal", "[Rn] 5f14 6d10 7s2 7p3", None, None, None, None, None, None, 15, 7, "Yuri Oganessian", 2004),
    (116, "Lv", "Livermorium", 293.0, "post-transition metal", "[Rn] 5f14 6d10 7s2 7p4", None, None, None, None, None, None, 16, 7, "Yuri Oganessian", 2000),
    (117, "Ts", "Tennessine", 294.0, "halogen", "[Rn] 5f14 6d10 7s2 7p5", None, None, None, None, None, None, 17, 7, "Yuri Oganessian", 2010),
    (118, "Og", "Oganesson", 294.0, "noble gas", "[Rn] 5f14 6d10 7s2 7p6", None, None, None, None, None, None, 18, 7, "Yuri Oganessian", 2006),
]

def build_elements():
    elements = []
    for e in elements_data:
        (z, symbol, name, mass, bowser_cat, e_config, en, radius, ie, density, melt, boil, group, period, discoverer, year) = e
        category = map_category(bowser_cat)
        el = {
            "atomicNumber": z,
            "symbol": symbol,
            "name": name,
            "atomicMass": mass,
            "category": category,
            "electronConfiguration": e_config,
            "electronegativity": en,
            "atomicRadius": radius,
            "ionizationEnergy": ie,
            "density": density,
            "meltingPoint": melt,
            "boilingPoint": boil,
            "group": group,
            "period": period,
            "discoveredBy": discoverer,
            "yearDiscovered": year,
        }
        elements.append(el)
    return {"elements": elements, "_source": "Bowserinator/Periodic-Table-JSON, CC BY-SA 3.0"}


# ============================================================
# Chinese names and pinyin for all 118 elements
# ============================================================
chinese_names = [
    (1, "氢", "qīng"),
    (2, "氦", "hài"),
    (3, "锂", "lǐ"),
    (4, "铍", "pí"),
    (5, "硼", "péng"),
    (6, "碳", "tàn"),
    (7, "氮", "dàn"),
    (8, "氧", "yǎng"),
    (9, "氟", "fú"),
    (10, "氖", "nǎi"),
    (11, "钠", "nà"),
    (12, "镁", "měi"),
    (13, "铝", "lǚ"),
    (14, "硅", "guī"),
    (15, "磷", "lín"),
    (16, "硫", "liú"),
    (17, "氯", "lǜ"),
    (18, "氩", "yà"),
    (19, "钾", "jiǎ"),
    (20, "钙", "gài"),
    (21, "钪", "kàng"),
    (22, "钛", "tài"),
    (23, "钒", "fán"),
    (24, "铬", "gè"),
    (25, "锰", "měng"),
    (26, "铁", "tiě"),
    (27, "钴", "gǔ"),
    (28, "镍", "niè"),
    (29, "铜", "tóng"),
    (30, "锌", "xīn"),
    (31, "镓", "jiā"),
    (32, "锗", "zhě"),
    (33, "砷", "shēn"),
    (34, "硒", "xī"),
    (35, "溴", "xiù"),
    (36, "氪", "kè"),
    (37, "铷", "rú"),
    (38, "锶", "sī"),
    (39, "钇", "yǐ"),
    (40, "锆", "gào"),
    (41, "铌", "ní"),
    (42, "钼", "mù"),
    (43, "锝", "dé"),
    (44, "钌", "liǎo"),
    (45, "铑", "lǎo"),
    (46, "钯", "bǎ"),
    (47, "银", "yín"),
    (48, "镉", "gé"),
    (49, "铟", "yīn"),
    (50, "锡", "xī"),
    (51, "锑", "tī"),
    (52, "碲", "dì"),
    (53, "碘", "diǎn"),
    (54, "氙", "xiān"),
    (55, "铯", "sè"),
    (56, "钡", "bèi"),
    (57, "镧", "lán"),
    (58, "铈", "shì"),
    (59, "镨", "pǔ"),
    (60, "钕", "nǚ"),
    (61, "钷", "pǒ"),
    (62, "钐", "shān"),
    (63, "铕", "yǒu"),
    (64, "钆", "gá"),
    (65, "铽", "tè"),
    (66, "镝", "dī"),
    (67, "钬", "huǒ"),
    (68, "铒", "ěr"),
    (69, "铥", "diū"),
    (70, "镱", "yì"),
    (71, "镥", "lǔ"),
    (72, "铪", "hā"),
    (73, "钽", "tǎn"),
    (74, "钨", "wū"),
    (75, "铼", "lái"),
    (76, "锇", "é"),
    (77, "铱", "yī"),
    (78, "铂", "bó"),
    (79, "金", "jīn"),
    (80, "汞", "gǒng"),
    (81, "铊", "tā"),
    (82, "铅", "qiān"),
    (83, "铋", "bì"),
    (84, "钋", "pō"),
    (85, "砹", "ài"),
    (86, "氡", "dōng"),
    (87, "钫", "fāng"),
    (88, "镭", "léi"),
    (89, "锕", "ā"),
    (90, "钍", "tǔ"),
    (91, "镤", "pú"),
    (92, "铀", "yóu"),
    (93, "镎", "ná"),
    (94, "钚", "bù"),
    (95, "镅", "méi"),
    (96, "锔", "jú"),
    (97, "锫", "péi"),
    (98, "锎", "kāi"),
    (99, "锿", "āi"),
    (100, "镄", "fèi"),
    (101, "钔", "mén"),
    (102, "锘", "nuò"),
    (103, "铹", "láo"),
    (104, "𬬻", "lú"),
    (105, "𬭊", "dù"),
    (106, "𬭳", "xǐ"),
    (107, "𬭛", "bō"),
    (108, "𬭶", "hēi"),
    (109, "鿏", "mài"),
    (110, "𫟼", "dá"),
    (111, "𬬭", "lún"),
    (112, "鿔", "gē"),
    (113, "鉨", "nǐ"),  # nihonium
    (114, "鈇", "fū"),  # flerovium
    (115, "鏌", "mò"),  # moscovium
    (116, "鉝", "lì"),  # livermorium
    (117, "鿬", "tián"), # tennessine
    (118, "鿫", "ào"),  # oganesson
]


def build_zh():
    result = []
    for z, name_zh, pinyin in chinese_names:
        result.append({
            "atomicNumber": z,
            "nameZh": name_zh,
            "pinyin": pinyin,
        })
    return result


if __name__ == "__main__":
    import os
    script_dir = os.path.dirname(os.path.abspath(__file__))
    proj_root = os.path.abspath(os.path.join(script_dir, ".."))
    assets_dir = os.path.join(proj_root, "app", "src", "main", "assets")
    os.makedirs(assets_dir, exist_ok=True)

    # Write elements.json
    elements_path = os.path.join(assets_dir, "elements.json")
    with open(elements_path, "w", encoding="utf-8") as f:
        json.dump(build_elements(), f, indent=2, ensure_ascii=False)
    print(f"Wrote {elements_path}")

    # Write elements_zh.json
    zh_path = os.path.join(assets_dir, "elements_zh.json")
    with open(zh_path, "w", encoding="utf-8") as f:
        json.dump(build_zh(), f, indent=2, ensure_ascii=False)
    print(f"Wrote {zh_path}")

    # Validate element count
    elements = build_elements()["elements"]
    print(f"Total elements: {len(elements)}")
    assert len(elements) == 118, f"Expected 118, got {len(elements)}"

    # Validate superheavy nulls (Z >= 104) — most are null except predicted values
    for el in elements:
        z = el["atomicNumber"]
        if z >= 104:
            # density is unknown for all superheavy elements
            assert el["density"] is None, f"Z={z} {el['symbol']} density should be None"

    # Validate zh
    zh_list = build_zh()
    assert len(zh_list) == 118, f"Expected 118 zh entries, got {len(zh_list)}"

    print("All validations passed!")
