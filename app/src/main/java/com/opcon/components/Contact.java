package com.opcon.components;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.Comparator;

/**
 *
 * Created by Mahmut Taşkiran on 06/12/2016.
 *
 */

public final class Contact implements Comparable<Contact>, Cloneable {

    public int lid;
    public String name, number;
    public String profileUri;
    public boolean hasOpcon;

    @Override public int hashCode() {
        if (number != null)
            return number.hashCode();
        return lid != 0 ? lid : super.hashCode();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return (lid != 0 && lid == contact.lid) || (number != null && number.equals(contact.number));
    }

    @Override public String toString() {
        return String.format("{name: '%s', phone: '%s' avatar: '%s'}",
                name, number, profileUri);
    }

    @Override public int compareTo(@NonNull Contact o) {
        return ContactComparator.compare(this, o);
    }

    private static Comparator<Contact> ContactComparator = new Comparator<Contact>() {
        private Collator instance = Collator.getInstance();
        @Override public int compare(Contact o1, Contact o2) {
            String name1 = o1.name.toLowerCase();
            String name2 = o2.name.toLowerCase();
            return instance.compare(name1, name2);
        }
    };

}
