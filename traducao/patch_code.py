"""Small code changes for the Brazilian personal build (not string translations).
Each replacement must match exactly once, so a changed upstream file fails loudly."""
import sys, os

root = sys.argv[1]

PATCHES = {
    'NavEngine.kt': [
        # kilometres by default, whatever country is detected (still changeable in Settings)
        ('prefs.s("distunit", if (defaultUnits() == "imperial") "mi" else "km")',
         'prefs.s("distunit", "km")'),
        ('return if (cc.isBlank() || cc in MILES_COUNTRIES) "imperial" else "metric"',
         'return if (cc.isNotBlank() && cc in MILES_COUNTRIES) "imperial" else "metric"'),
        # Brazil as the default offline pack (instead of England & Wales)
        ('return PackRegions.suggestFor(detectedCountry) ?: PackRegions.byId("ew")',
         'return PackRegions.suggestFor(detectedCountry) ?: PackRegions.byId("brazil")'),
        ('fun updatePack() = downloadRegion(PackRegions.byId(installedPackId() ?: prefs.s("packregion", "ew")))',
         'fun updatePack() = downloadRegion(PackRegions.byId(installedPackId() ?: prefs.s("packregion", "brazil")))'),
    ],
    'ui/SettingsScreen.kt': [
        ('val cc = engine.chosenPack().country.ifBlank { engine.detectedCountry }.ifBlank { "GB" }',
         'val cc = engine.chosenPack().country.ifBlank { engine.detectedCountry }.ifBlank { "BR" }'),
    ],
}

extra = sys.argv[2] if len(sys.argv) > 2 else None
if extra:
    exec(open(extra, encoding='utf-8').read())  # adds more entries to PATCHES

bad = False
for rel, reps in PATCHES.items():
    p = os.path.join(root, rel)
    t = open(p, encoding='utf-8').read()
    for old, new in reps:
        if old.startswith('@ALL@'):
            old = old[5:]
            if old not in t:
                print(f'PATCH MISS (0x) in {rel}: {old[:80]}'); bad = True
            t = t.replace(old, new)
            continue
        n = t.count(old)
        if n != 1:
            print(f'PATCH MISS ({n}x) in {rel}: {old[:80]}')
            bad = True
            continue
        t = t.replace(old, new)
    open(p, 'w', encoding='utf-8').write(t)
if bad:
    sys.exit(1)
print('code patches ok')
