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

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.mosaicnetworks.babble.fragments.SelectableData;
import io.mosaicnetworks.babble.node.ConfigDirectory;
import io.mosaicnetworks.babble.node.ConfigManager;

/**
 * This class extends {@link ViewModel} and exposes methods to populate
 * the list of archive groups.
 */
public class ArchivedGroupsViewModel extends ViewModel {

    private MutableLiveData<SelectableData<ConfigDirectory>> mArchivedList;
    private ConfigManager mConfigManager;

    /**
     * Constructor that set the instance of {@link ConfigManager} used to populate
     * the archive group list.
     *
     * @param configManager a ConfigManager instance
     */
    public ArchivedGroupsViewModel(ConfigManager configManager) {
        super();
        mConfigManager = configManager;

        mArchivedList = new MutableLiveData<>();
        loadArchiveList();
    }

    /**
     * This method populates the mArchivedList global with Archive Group data
     */
    public void loadArchiveList() {
        SelectableData<ConfigDirectory> data = new SelectableData<>();
        data.addAll(mConfigManager.getDirectories());
        mArchivedList.setValue(data);
    }

    /**
     * Getter to retrieve the list populated by {@link #loadArchiveList()}
     * @return
     */
    public MutableLiveData<SelectableData<ConfigDirectory>> getArchivedList() {
        return mArchivedList;
    }

}

