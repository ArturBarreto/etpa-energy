package com.etpa.energy.domain;

/**
 * Calendar months. The legacy data references 3-letter upper-case names.
 * We also provide a helper to get the previous month (JAN has no previous: yearly reset).
 */
public enum Month {
    JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC;

    /** Previous calendar month; JAN has no previous because meters reset on Jan 1st (base=0). */
    public Month previous() {
        return switch (this) {
            case JAN -> null;
            case FEB -> JAN;
            case MAR -> FEB;
            case APR -> MAR;
            case MAY -> APR;
            case JUN -> MAY;
            case JUL -> JUN;
            case AUG -> JUL;
            case SEP -> AUG;
            case OCT -> SEP;
            case NOV -> OCT;
            case DEC -> NOV;
        };
    }

    /** Parse like "jan"/" JAN " safely. */
    public static Month fromString(String v) {
        return Month.valueOf(v.trim().toUpperCase());
    }
}
