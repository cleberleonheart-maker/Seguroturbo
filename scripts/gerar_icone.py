#!/usr/bin/env python3
import sys
import os
import zlib
import struct

W = H = 512
SS = 4
FUNDO = (5,7,10)
ESCUDO = (0,242,255)
VP = 108.0
ESCALA = W/VP

def cubica(p0,p1,p2,p3,n=24):
    pts=[]
    for i in range(1,n+1):
        t = i/n
        mt = 1-t
        x = mt**3*p0[0] + 3*mt**2*t*p1[0] + 3*mt*t**2*p2[0] + t**3*p3[0]
        y = mt**3*p0[1] + 3*mt**2*t*p1[1] + 3*mt*t**2*p2[1] + t**3*p3[1]
        pts.append((x,y))
    return pts

EC1 = cubica((80,54),(80,72),(67,85),(54,90))
EC2 = cubica((54,90),(41,85),(28,72),(28,54))
ESC = [(54,20),(80,29),(80,54)]+EC1+EC2+[(28,29)]
RAIO = [(57,33),(43,58),(53,58),(50,77),(66,50),(56,50)]

def px(p):
    return [(x*ESCALA,y*ESCALA) for x,y in p]

def fill(buf,w,h,poly,c):
    n = len(poly)
    ymin = max(0,int(min(pt[1] for pt in poly)))
    ymax = min(h-1,int(max(pt[1] for pt in poly))+1)
    for y in range(ymin, ymax+1):
        yc = y+0.5
        xs = []
        for i in range(n):
            x0,y0 = poly[i]; x1,y1 = poly[(i+1)%n]
            if (y0 <= yc < y1) or (y1 <= yc < y0):
                t = (yc-y0)/(y1-y0) if (y1 != y0) else 0.0
                xs.append((x0+t*(x1-x0), 1 if y1>y0 else -1))
        if not xs:
            continue
        xs.sort()
        var = 0; ini = None
        for x,dr in xs:
            if var == 0:
                ini = x
            var += dr
            if var == 0 and ini is not None:
                xi = max(0,int(round(ini)))
                xf = min(w,int(round(x)))
                for xp in range(xi,xf):
                    buf[y*w+xp] = c
                ini = None

def png(path,pixels,w,h):
    raw = b""
    for y in range(h):
        row = pixels[y*w:(y+1)*w]
        raw += b"\x00"
        for px in row:
            raw += bytes([px[0],px[1],px[2]])
    def ctyp(t,d):
        ch = t+d
        return struct.pack(">I",len(d))+ch+struct.pack(">I",zlib.crc32(ch))
    ihdr = struct.pack(">IIBBBBB",w,h,8,2,0,0,0)
    with open(path,"wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(ctyp(b"IHDR",ihdr))
        f.write(ctyp(b"IDAT",zlib.compress(raw,9)))
        f.write(ctyp(b"IEND",b""))

def main():
    for lang in ["pt-BR","en-US","es-ES","fr-FR"]:
        out = f"fastlane/metadata/android/{lang}/images/icon.png"
        os.makedirs(os.path.dirname(out), exist_ok=True)
        ws,hs = W*SS,H*SS
        buf = [FUNDO]*ws*hs
        fill(buf,ws,hs,px(ESC),ESCUDO)
        fill(buf,ws,hs,px(RAIO),FUNDO)
        pixels = []
        for y in range(H):
            for x in range(W):
                r=g=b=0
                for dy in range(SS):
                    base = (y*SS+dy)*ws + x*SS
                    for dx in range(SS):
                        p = buf[base+dx]
                        r+=p[0]; g+=p[1]; b+=p[2]
                pixels.append((r//(SS*SS), g//(SS*SS), b//(SS*SS)))
        png(out,pixels,W,H)
        print(f"OK: {out}")

if __name__=="__main__":
    main()
