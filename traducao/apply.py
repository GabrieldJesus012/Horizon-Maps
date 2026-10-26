"""apply.py ROOT MAPFILE...: replace whole string literals per file.

Map format:
  @@ relative/path.kt
  english raw ⇒ portuguese raw
  !skip LINE        (optional: don't touch literals on that line)
Only literals whose raw text equals a map key exactly are replaced.
Reports keys that matched nothing (likely typos).
"""
import sys, os
sys.path.insert(0, '/home/claude/tools')
from ktstrings import scan

root = sys.argv[1]
sections = {}
cur = None
for mf in sys.argv[2:]:
    for line in open(mf, encoding='utf-8'):
        line = line.rstrip('\n')
        if line.startswith('@@ '):
            cur = sections.setdefault(line[3:].strip(), {'map': {}, 'skip': set()})
            continue
        if not line.strip():
            continue
        if line.startswith('!skip '):
            cur['skip'].update(int(x) for x in line[6:].split(','))
            continue
        if ' ⇒ ' not in line:
            print('BAD LINE', mf, repr(line)); continue
        en, pt = line.split(' ⇒ ', 1)
        if en in cur['map'] and cur['map'][en] != pt:
            print('CONFLICT', en)
        cur['map'][en] = pt

total = 0
for rel, sec in sections.items():
    path = os.path.join(root, rel)
    t = open(path, encoding='utf-8').read()
    used = set()
    out, last = [], 0
    for s in scan(t):
        if s['kind'] != 'str' or s['raw'] not in sec['map']:
            continue
        ln = t.count('\n', 0, s['start']) + 1
        if ln in sec['skip']:
            continue
        out.append(t[last:s['start']])
        out.append('"' + sec['map'][s['raw']] + '"')
        last = s['end']
        used.add(s['raw'])
        total += 1
    out.append(t[last:])
    new = ''.join(out)
    # sanity: the rewritten file must still scan cleanly
    scan(new)
    open(path, 'w', encoding='utf-8').write(new)
    for k in sec['map']:
        if k not in used:
            print('UNUSED', rel, repr(k))
print('replaced', total)
