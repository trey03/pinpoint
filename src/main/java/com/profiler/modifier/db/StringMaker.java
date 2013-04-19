package com.profiler.modifier.db;

/**
 *
 */
public class StringMaker {

    /**
     * The value.
     */
    private String value;

    /**
     * The indexing.
     */
    private String indexing;

    /**
     * The begin.
     */
    private int begin;

    /**
     * The end.
     */
    private int end;

    /**
     * Instantiates a new string maker.
     *
     * @param value the value
     */
    public StringMaker(String value) {
        this.value = value;
        this.indexing = value;
        this.end = value.length();
    }

    /**
     * Instantiates a new string maker.
     *
     * @param value the value
     * @param begin the begin
     * @param end   the end
     */
    private StringMaker(String value, int begin, int end) {
        this.value = value;
        this.indexing = value;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Lower.
     *
     * @return the string maker
     */
    public StringMaker lower() {
        indexing = indexing.toLowerCase();
        return this;
    }

    /**
     * Upper.
     *
     * @return the string maker
     */
    public StringMaker upper() {
        indexing = indexing.toUpperCase();
        return this;
    }

    /**
     * Reset.
     *
     * @return the string maker
     */
    public StringMaker reset() {
        indexing = value;
        return this;
    }

    /**
     * After.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker after(char ch) {
        int index = indexing.indexOf(ch, begin);

        if (index < 0 || index > end) {
            return this;
        }

        begin = index + 1 > end ? end : index + 1;
        return this;
    }

    /**
     * After.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker after(String ch) {
        int index = indexing.indexOf(ch, begin);

        if (index < 0 || index > end) {
            return this;
        }

        begin = index + ch.length() > end ? end : index + ch.length();
        return this;
    }

    /**
     * Before.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker before(char ch) {
        int index = indexing.indexOf(ch, begin);

        if (index < 0 || index > end) {
            return this;
        }

        end = index < begin ? begin : index;
        return this;
    }

    /**
     * Before.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker before(String ch) {
        int index = indexing.indexOf(ch, begin);

        if (index < 0 || index > end) {
            return this;
        }

        end = index < begin ? begin : index;
        return this;
    }

    /**
     * After last.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker afterLast(char ch) {
        int index = indexing.lastIndexOf(ch, end);

        if (index < begin) {
            return this;
        }

        begin = index + 1 > end ? end : index + 1;
        return this;
    }

    /**
     * After last.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker afterLast(String ch) {
        int index = indexing.lastIndexOf(ch, end);

        if (index < begin) {
            return this;
        }

        begin = index + ch.length() > end ? end : index + ch.length();
        return this;
    }

    /**
     * Before last.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker beforeLast(char ch) {
        int index = indexing.lastIndexOf(ch, end);

        if (index < begin) {
            return this;
        }

        //end = index < begin ? begin : index;
        //for Klocwork

        end = index;
        return this;
    }

    /**
     * Before last.
     *
     * @param ch the ch
     * @return the string maker
     */
    public StringMaker beforeLast(String ch) {
        int index = indexing.lastIndexOf(ch, end);

        if (index < begin) {
            return this;
        }

        //end = index < begin ? begin : index;
        //for Klocwork

        end = index;
        return this;
    }

    /**
     * Prev.
     *
     * @return the string maker
     */
    public StringMaker prev() {
        this.end = begin;
        this.begin = 0;
        return this;
    }

    /**
     * Next.
     *
     * @return the string maker
     */
    public StringMaker next() {
        this.begin = end;
        this.end = indexing.length();
        return this;
    }

    /**
     * Clear.
     *
     * @return the string maker
     */
    public StringMaker clear() {
        begin = 0;
        end = indexing.length();
        return this;
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return begin == end;
    }

    /**
     * Value.
     *
     * @return the string
     */
    public String value() {
        return value.substring(begin, end);
    }

    /**
     * Dulicate.
     *
     * @return the string maker
     */
    public StringMaker duplicate() {
        return new StringMaker(value, begin, end);
    }

    /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */

    /**
     * To string.
     *
     * @return value() String
     */
    public String toString() {
        return value();
    }
}