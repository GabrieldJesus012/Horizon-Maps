# regenerate maps/part6_index.txt from the settings screen maps + index-only extras
pairs={}
for f in ['part4.txt','part5.txt','part2.txt']:
    cur=None
    for line in open('mapas/'+f,encoding='utf-8'):
        line=line.rstrip('\n')
        if line.startswith('@@ '): cur=line[3:]; continue
        if cur in ('ui/SettingsScreen.kt','ui/OfflineMapsPage.kt') and ' ⇒ ' in line:
            en,pt=line.split(' ⇒ ',1); pairs.setdefault(en,pt)
for line in open('index_extra.txt',encoding='utf-8'):
    line=line.rstrip('\n')
    if ' ⇒ ' in line:
        en,pt=line.split(' ⇒ ',1); pairs[en]=pt
with open('mapas/part6_index.txt','w',encoding='utf-8') as o:
    o.write('@@ ui/SettingsIndex.kt\n')
    for en,pt in pairs.items(): o.write(f'{en} ⇒ {pt}\n')
