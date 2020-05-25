/*
 * MIT License
 *
 * Copyright (c) 2018- Mosaic Networks
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.mosaicnetworks.babble.fragments.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds a list of objects which, for each object, has a flag denoting whether the object is
 * selected or not. The selected state is controlled by the user of this class.
 * @param <T> is the type of the collection of objects
 */
public final class SelectableData<T> {

    private final List<T> mData = new ArrayList<>();
    private final List<Boolean> mSelectedFlags = new ArrayList<>();
    private final List<Integer> mAllSelected = new ArrayList<>();

    /**
     * Set selected flag to true for item at the given position
     * @param position is the item's position in the list
     */
    public void select(int position) {
        if (!mSelectedFlags.get(position)) {
            mAllSelected.add(position);
            mSelectedFlags.set(position, true);
        }
    }

    /**
     * Set selected flag to false for item at the given position
     * @param position is the item's position in the list
     */
    public void unSelect(int position) {
        if (mSelectedFlags.get(position)) {
            mSelectedFlags.set(position, false);
            mAllSelected.remove(new Integer(position));
        }
    }

    /**
     * Return whether the item at the given position it selected
     * @param position is the item's position in the list
     * @return true if item is selected, otherwise false
     */
    public Boolean isSelected(int position) {
        return mSelectedFlags.get(position);
    }

    /**
     * Unselects all items
     */
    public void unSelectAll() {
        for (Integer position:mAllSelected) {
            mSelectedFlags.set(position, false);
        }

        mAllSelected.clear();
    }

    /**
     * Returns a list of the positions of all the selected items
     * @return {@link List} of the positions of all selected items
     */
    public List<Integer> getAllSelected() {
        return new ArrayList<>(mAllSelected); //return a copy so the list cannot be interfered with
    }

    /**
     * Returns true if any items are selected, false otherwise
     * @return tru if any items selected, false otherwise
     */
    public boolean anySelected() {
        return !mAllSelected.isEmpty();
    }

    /**
     * Remove all selected items from the list
     */
    public void removeAllSelected() {
        Collections.sort(mAllSelected);
        Collections.reverse(mAllSelected);

        for (int selPos:mAllSelected) {
            mData.remove(selPos);
            mSelectedFlags.remove(selPos);
        }

        mAllSelected.clear();
    }

    /**
     * Return true if the list is empty, false otherwise
     * @return true if list is empty, false otherwise
     */
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    /**
     * Append items to the end of the list
     * @param items the list of items to append to the list
     */
    public void addAll(List<T> items) {
        mData.addAll(items);
        mSelectedFlags.addAll(Collections.nCopies(items.size(), false));
    }

    /**
     * Append an item to the end of the list
     * @param item Item to add to the list
     */
    public void add(T item) {
        mData.add(item);
        mSelectedFlags.add(false);
    }

    /**
     * Remove all items from the list
     */
    public void clear() {
        mData.clear();
        mSelectedFlags.clear();
        mAllSelected.clear();
    }

    /**
     * Get item at position
     * @param position the position of the item to return
     * @return the item at the specified position
     */
    public T get(int position) {
        return mData.get(position);
    }

    /**
     * get the size of the list
     * @return the size of the list
     */
    public int size() {
        return mData.size();
    }
}
