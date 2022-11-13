/*
 * Copyright (C) 1999-2001, 2016 Free Software Foundation, Inc.
 * This file is part of the GNU LIBICONV Library.
 *
 * The GNU LIBICONV Library is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * The GNU LIBICONV Library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with the GNU LIBICONV Library; see the file COPYING.LIB.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * MacCentralEurope
 */

static const unsigned short mac_centraleurope_2uni[128] = {
  /* 0x80 */
  0x00c4, 0x0100, 0x0101, 0x00c9, 0x0104, 0x00d6, 0x00dc, 0x00e1,
  0x0105, 0x010c, 0x00e4, 0x010d, 0x0106, 0x0107, 0x00e9, 0x0179,
  /* 0x90 */
  0x017a, 0x010e, 0x00ed, 0x010f, 0x0112, 0x0113, 0x0116, 0x00f3,
  0x0117, 0x00f4, 0x00f6, 0x00f5, 0x00fa, 0x011a, 0x011b, 0x00fc,
  /* 0xa0 */
  0x2020, 0x00b0, 0x0118, 0x00a3, 0x00a7, 0x2022, 0x00b6, 0x00df,
  0x00ae, 0x00a9, 0x2122, 0x0119, 0x00a8, 0x2260, 0x0123, 0x012e,
  /* 0xb0 */
  0x012f, 0x012a, 0x2264, 0x2265, 0x012b, 0x0136, 0x2202, 0x2211,
  0x0142, 0x013b, 0x013c, 0x013d, 0x013e, 0x0139, 0x013a, 0x0145,
  /* 0xc0 */
  0x0146, 0x0143, 0x00ac, 0x221a, 0x0144, 0x0147, 0x2206, 0x00ab,
  0x00bb, 0x2026, 0x00a0, 0x0148, 0x0150, 0x00d5, 0x0151, 0x014c,
  /* 0xd0 */
  0x2013, 0x2014, 0x201c, 0x201d, 0x2018, 0x2019, 0x00f7, 0x25ca,
  0x014d, 0x0154, 0x0155, 0x0158, 0x2039, 0x203a, 0x0159, 0x0156,
  /* 0xe0 */
  0x0157, 0x0160, 0x201a, 0x201e, 0x0161, 0x015a, 0x015b, 0x00c1,
  0x0164, 0x0165, 0x00cd, 0x017d, 0x017e, 0x016a, 0x00d3, 0x00d4,
  /* 0xf0 */
  0x016b, 0x016e, 0x00da, 0x016f, 0x0170, 0x0171, 0x0172, 0x0173,
  0x00dd, 0x00fd, 0x0137, 0x017b, 0x0141, 0x017c, 0x0122, 0x02c7,
};

static int
mac_centraleurope_mbtowc (conv_t conv, ucs4_t *pwc, const unsigned char *s, size_t n)
{
  unsigned char c = *s;
  if (c < 0x80)
    *pwc = (ucs4_t) c;
  else
    *pwc = (ucs4_t) mac_centraleurope_2uni[c-0x80];
  return 1;
}

static const unsigned char mac_centraleurope_page00[224] = {
  0xca, 0x00, 0x00, 0xa3, 0x00, 0x00, 0x00, 0xa4, /* 0xa0-0xa7 */
  0xac, 0xa9, 0x00, 0xc7, 0xc2, 0x00, 0xa8, 0x00, /* 0xa8-0xaf */
  0xa1, 0x00, 0x00, 0x00, 0x00, 0x00, 0xa6, 0x00, /* 0xb0-0xb7 */
  0x00, 0x00, 0x00, 0xc8, 0x00, 0x00, 0x00, 0x00, /* 0xb8-0xbf */
  0x00, 0xe7, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, /* 0xc0-0xc7 */
  0x00, 0x83, 0x00, 0x00, 0x00, 0xea, 0x00, 0x00, /* 0xc8-0xcf */
  0x00, 0x00, 0x00, 0xee, 0xef, 0xcd, 0x85, 0x00, /* 0xd0-0xd7 */
  0x00, 0x00, 0xf2, 0x00, 0x86, 0xf8, 0x00, 0xa7, /* 0xd8-0xdf */
  0x00, 0x87, 0x00, 0x00, 0x8a, 0x00, 0x00, 0x00, /* 0xe0-0xe7 */
  0x00, 0x8e, 0x00, 0x00, 0x00, 0x92, 0x00, 0x00, /* 0xe8-0xef */
  0x00, 0x00, 0x00, 0x97, 0x99, 0x9b, 0x9a, 0xd6, /* 0xf0-0xf7 */
  0x00, 0x00, 0x9c, 0x00, 0x9f, 0xf9, 0x00, 0x00, /* 0xf8-0xff */
  /* 0x0100 */
  0x81, 0x82, 0x00, 0x00, 0x84, 0x88, 0x8c, 0x8d, /* 0x00-0x07 */
  0x00, 0x00, 0x00, 0x00, 0x89, 0x8b, 0x91, 0x93, /* 0x08-0x0f */
  0x00, 0x00, 0x94, 0x95, 0x00, 0x00, 0x96, 0x98, /* 0x10-0x17 */
  0xa2, 0xab, 0x9d, 0x9e, 0x00, 0x00, 0x00, 0x00, /* 0x18-0x1f */
  0x00, 0x00, 0xfe, 0xae, 0x00, 0x00, 0x00, 0x00, /* 0x20-0x27 */
  0x00, 0x00, 0xb1, 0xb4, 0x00, 0x00, 0xaf, 0xb0, /* 0x28-0x2f */
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xb5, 0xfa, /* 0x30-0x37 */
  0x00, 0xbd, 0xbe, 0xb9, 0xba, 0xbb, 0xbc, 0x00, /* 0x38-0x3f */
  0x00, 0xfc, 0xb8, 0xc1, 0xc4, 0xbf, 0xc0, 0xc5, /* 0x40-0x47 */
  0xcb, 0x00, 0x00, 0x00, 0xcf, 0xd8, 0x00, 0x00, /* 0x48-0x4f */
  0xcc, 0xce, 0x00, 0x00, 0xd9, 0xda, 0xdf, 0xe0, /* 0x50-0x57 */
  0xdb, 0xde, 0xe5, 0xe6, 0x00, 0x00, 0x00, 0x00, /* 0x58-0x5f */
  0xe1, 0xe4, 0x00, 0x00, 0xe8, 0xe9, 0x00, 0x00, /* 0x60-0x67 */
  0x00, 0x00, 0xed, 0xf0, 0x00, 0x00, 0xf1, 0xf3, /* 0x68-0x6f */
  0xf4, 0xf5, 0xf6, 0xf7, 0x00, 0x00, 0x00, 0x00, /* 0x70-0x77 */
  0x00, 0x8f, 0x90, 0xfb, 0xfd, 0xeb, 0xec, 0x00, /* 0x78-0x7f */
};
static const unsigned char mac_centraleurope_page20[48] = {
  0x00, 0x00, 0x00, 0xd0, 0xd1, 0x00, 0x00, 0x00, /* 0x10-0x17 */
  0xd4, 0xd5, 0xe2, 0x00, 0xd2, 0xd3, 0xe3, 0x00, /* 0x18-0x1f */
  0xa0, 0x00, 0xa5, 0x00, 0x00, 0x00, 0xc9, 0x00, /* 0x20-0x27 */
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x28-0x2f */
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x30-0x37 */
  0x00, 0xdc, 0xdd, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x38-0x3f */
};
static const unsigned char mac_centraleurope_page22[32] = {
  0x00, 0x00, 0xb6, 0x00, 0x00, 0x00, 0xc6, 0x00, /* 0x00-0x07 */
  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x08-0x0f */
  0x00, 0xb7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x10-0x17 */
  0x00, 0x00, 0xc3, 0x00, 0x00, 0x00, 0x00, 0x00, /* 0x18-0x1f */
};
static const unsigned char mac_centraleurope_page22_1[8] = {
  0xad, 0x00, 0x00, 0x00, 0xb2, 0xb3, 0x00, 0x00, /* 0x60-0x67 */
};

static int
mac_centraleurope_wctomb (conv_t conv, unsigned char *r, ucs4_t wc, size_t n)
{
  unsigned char c = 0;
  if (wc < 0x0080) {
    *r = wc;
    return 1;
  }
  else if (wc >= 0x00a0 && wc < 0x0180)
    c = mac_centraleurope_page00[wc-0x00a0];
  else if (wc == 0x02c7)
    c = 0xff;
  else if (wc >= 0x2010 && wc < 0x2040)
    c = mac_centraleurope_page20[wc-0x2010];
  else if (wc == 0x2122)
    c = 0xaa;
  else if (wc >= 0x2200 && wc < 0x2220)
    c = mac_centraleurope_page22[wc-0x2200];
  else if (wc >= 0x2260 && wc < 0x2268)
    c = mac_centraleurope_page22_1[wc-0x2260];
  else if (wc == 0x25ca)
    c = 0xd7;
  if (c != 0) {
    *r = c;
    return 1;
  }
  return RET_ILUNI;
}
