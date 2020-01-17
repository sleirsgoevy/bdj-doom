// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id:$
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This source is available for distribution and/or modification
// only under the terms of the DOOM Source Code License as
// published by id Software. All rights reserved.
//
// The source is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// FITNESS FOR A PARTICULAR PURPOSE. See the DOOM Source Code License
// for more details.
//
// $Log:$
//
// DESCRIPTION:
//	Endianess handling, swapping 16bit and 32bit.
//
//-----------------------------------------------------------------------------

static const char
rcsid[] = "$Id: m_bbox.c,v 1.1 1997/02/03 22:45:10 b1 Exp $";


#ifdef __GNUG__
#pragma implementation "m_swap.h"
#endif
#include "m_swap.h"


// Not needed with little endian.
#ifdef __BIG_ENDIAN__

// Swap 16bit, that is, MSB and LSB byte.
short ReadSHORT(short* addr)
{
    unsigned char* p = (char*)addr;
    return p[0] | p[1] << 8;
}

// Swapping 32bit.
long ReadLONG(long* addr)
{
    unsigned char* p = (char*)addr;
    unsigned long ans = 0;
    for(int i = 3; i >= 0; i--)
        ans = ans << 8 | p[i];
    return ans;
}


#endif


