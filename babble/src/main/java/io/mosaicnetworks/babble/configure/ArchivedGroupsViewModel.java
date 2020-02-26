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

package io.mosaicnetworks.babble.configure;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.mosaicnetworks.babble.node.BabbleService;
import io.mosaicnetworks.babble.node.GroupDescriptor;

public class ArchivedGroupsViewModel extends ViewModel {

    //TODO: Review theses states for completeness; possibly extra state for leaving?
    public enum State {
        LIST,
        LOADING,
        LOADED,
        FAILED,
    }

    private MutableLiveData<State> mState;
    private BabbleService mBabbleService;
    private String mConfigDirectory;
    private GroupDescriptor mGroupDescriptor;

    public ArchivedGroupsViewModel(BabbleService babbleService) {
        super();
        mBabbleService = babbleService;

        mState = new MutableLiveData<>();
        mState.setValue(State.LIST);
    }

    public void loadArchive(String configDirectory, GroupDescriptor groupDescriptor) {
        mState.setValue(State.LOADING);

        mBabbleService.startArchive(configDirectory, groupDescriptor, new BabbleService.StartArchiveListener() {
            @Override
            public void onInitialised() {
                mState.postValue(State.LOADED);
            }

            @Override
            public void onFailed() {
                mState.postValue(State.FAILED);
            }
        });
    }

    public MutableLiveData<State> getState() {
        return mState;
    }

}

