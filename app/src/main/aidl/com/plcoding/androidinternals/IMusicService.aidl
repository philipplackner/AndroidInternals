package com.plcoding.androidinternals;

import com.plcoding.androidinternals.ISongNameChangedCallback;

interface IMusicService {
    void next();
    void previous();
    String getCurrentSongName();

    void registerCallback(ISongNameChangedCallback callback);
    void unregisterCallback(ISongNameChangedCallback callback);
}