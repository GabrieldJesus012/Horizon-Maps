"""Kotlin string-literal scanner.

scan(text) -> list of dicts: {start, end, raw, kind}
  start/end: offsets of the literal INCLUDING its quotes
  raw: the text between the quotes (templates kept verbatim)
  kind: 'str' or 'raw' (triple-quoted)
Handles // and /* */ comments (nested), char literals, ${...} templates
with nested strings and braces.
"""
import sys, json, re


def _skip_template(t, i):
    # t[i] is just after "${"; return index just after matching "}"
    depth = 1
    n = len(t)
    while i < n:
        c = t[i]
        if c == '"':
            if t.startswith('"""', i):
                i = _skip_raw(t, i + 3)
            else:
                i, _ = _skip_str(t, i + 1)
            continue
        if c == "'":
            i = _skip_char(t, i + 1)
            continue
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1
    raise ValueError("unterminated template")


def _skip_str(t, i):
    # i just after opening quote; return (index after closing quote, content end)
    n = len(t)
    while i < n:
        c = t[i]
        if c == '\\':
            i += 2
            continue
        if c == '$' and i + 1 < n and t[i + 1] == '{':
            i = _skip_template(t, i + 2)
            continue
        if c == '"':
            return i + 1, i
        if c == '\n':
            raise ValueError("newline in string at %d" % i)
        i += 1
    raise ValueError("unterminated string")


def _skip_raw(t, i):
    n = len(t)
    while i < n:
        if t[i] == '$' and i + 1 < n and t[i + 1] == '{':
            i = _skip_template(t, i + 2)
            continue
        if t.startswith('"""', i):
            j = i + 3
            while j < n and t[j] == '"':
                j += 1
            return j
        i += 1
    raise ValueError("unterminated raw string")


def _skip_char(t, i):
    # i just after opening '
    if t[i] == '\\':
        j = t.index("'", i + 2)
        return j + 1
    return i + 2


def scan(t):
    out = []
    i, n = 0, len(t)
    while i < n:
        c = t[i]
        if t.startswith('//', i):
            i = t.find('\n', i)
            if i < 0:
                break
            continue
        if t.startswith('/*', i):
            depth = 1
            i += 2
            while depth and i < n:
                if t.startswith('/*', i):
                    depth += 1; i += 2
                elif t.startswith('*/', i):
                    depth -= 1; i += 2
                else:
                    i += 1
            continue
        if c == "'":
            i = _skip_char(t, i + 1)
            continue
        if c == '"':
            if t.startswith('"""', i):
                j = _skip_raw(t, i + 3)
                out.append(dict(start=i, end=j, raw=t[i + 3:j - 3], kind='raw'))
                i = j
            else:
                j, ce = _skip_str(t, i + 1)
                out.append(dict(start=i, end=j, raw=t[i + 1:ce], kind='str'))
                i = j
            continue
        i += 1
    return out


if __name__ == '__main__':
    for path in sys.argv[1:]:
        t = open(path, encoding='utf-8').read()
        for s in scan(t):
            line = t.count('\n', 0, s['start']) + 1
            print(json.dumps(dict(file=path, line=line, raw=s['raw'], kind=s['kind']), ensure_ascii=False))
