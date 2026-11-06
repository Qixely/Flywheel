package io.benwiegand.projection.libprivd;

import io.benwiegand.projection.libprivd.audio.AudioCaptureResult;

parcelable AudioCaptureResult;

interface IPrivd {
    void ping();

    boolean injectInputEvent(in InputEvent event);

    boolean injectInputEventWithDisplayId(in InputEvent event, int displayId);

    int launchActivity(in ComponentName component, int displayId);

    int launchVirtualActivity(in ComponentName component, int displayId);

    int createVirtualDisplay(in String name, int width, int height, int densityDpi, in Surface surface, int flags);

    void releaseVirtualDisplay(int displayId);

    void virtualDisplayResize(int displayId, int width, int height, int densityDpi);

    void virtualDisplaySetSurface(int displayId, in Surface surface);

    int createPrivilegedAudioRecordCapture(in AudioFormat audioFormat, int bufferSize, int audioSource);

    void destroyAudioCapture(int id);

    void audioCaptureBegin(int id);

    void audioCaptureNextBuffer(int id, out AudioCaptureResult result, out byte[] buffer, int offset, int length);

    // Appended so existing AIDL transaction numbers remain unchanged.
    // Switch only the phone panel, leaving the virtual projection display active.
    boolean setPhysicalDisplayPower(boolean on);

    // Safe on daemon shutdown: restore only if this daemon powered it off.
    void restorePhysicalDisplayPowerIfNeeded();
}
