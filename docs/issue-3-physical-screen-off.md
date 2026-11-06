# Experimental phone-panel power control for Flywheel issue #3

This is an **opt-in, untested feature branch** based on upstream v1.1.1 (`a0894e5`).
It is unrelated to the Qixely `palma2pro-working` integration branch.

## Motivation / design

Android's ordinary screen sleep can suspend the virtual display used by
Flywheel, resulting in frozen projection video and head-unit input. Hiding the
phone UI behind black pixels only helps OLED panels and does not turn an LCD
backlight off. The alternative implemented here keeps Flywheel's existing
`ScreenWakeActivity` active (and Android interactive), while asking the
privileged shell/root daemon to change **only the physical panel's power mode**.
The projection display is not targeted.

This follows the same general principle as scrcpy's `--turn-screen-off` mode.

## Behavior

- Disabled by default. Enable **Prevent display sleep while connected**, then
  **Power off phone display during projection (experimental)** in compatibility
  settings.
- When Flywheel launches its existing wake Activity after the phone is locked,
  a local ready notification schedules physical panel-off after 750 ms.
- On Activity pause/unlock, projection suspend/disconnect, and service destroy,
  the app requests panel-on before stopping the wake Activity.
- The privileged daemon remembers whether it switched the panel off, and
  attempts restoration before normal daemon teardown and its existing
  5-second ping-timeout exit.
- If panel power control fails, normal wake Activity behavior remains.

## Platform support

- Android 15+ (API 35+): tries `cmd display power-off 0` / `power-on 0` via
  the shell/root daemon (documented by scrcpy).
- Earlier Android: best-effort hidden `SurfaceControl` reflection. Some
  Android 14 devices moved display-token APIs to `DisplayControl`, which may
  not be accessible from the privileged process; failure is logged and the
  old wake Activity remains in place. **Compatibility is not guaranteed.**
- Requires a working Shizuku/root privileged daemon; the app itself cannot
  manipulate the physical panel as an ordinary unprivileged app.

## Test matrix (not yet device-tested)

1. Confirm default settings preserve previous behavior.
2. Enable the experimental option, connect to a head unit, lock the phone.
   Confirm the phone panel/backlight turns off, while Waze/video, audio, and
   the head-unit touch controls still work for 5+ minutes.
3. Press the physical power button. Confirm the phone screen can be used again;
   repeat the projection/lock sequence afterward.
4. Disconnect USB / suspend projection. Confirm the phone screen is restored.
5. Kill/stop Flywheel while connected and check the daemon's timeout recovery.
6. Test failure modes (no Shizuku; unsupported Android/vendor ROM) and confirm
   the old keep-awake screen is used without a crash.
7. Test sleep/lock security: the device should remain behind the existing
   secure keyguard, and lock notifications/privacy should not be bypassed.

## Recovery if a vendor leaves the phone display dark

Disconnect projection. Press the physical power button twice, allowing a moment
between presses. On Android 15+, if ADB is available, run
`adb shell cmd display power-on 0`. Reboot only if needed. Do not enable this
option on a daily-driver phone without first verifying the recovery procedure.

## Limitations

- The shell `cmd display` interface and hidden `SurfaceControl` methods have
  vendor- and version-specific behavior. Physical-device testing is mandatory
  before this is considered resolved.
- This approach powers down the physical panel but does **not** force the
  Android framework to suspend/lock; Flywheel preserves its current lockscreen
  handling in `ScreenWakeActivity`.
- A hard kill of the privileged daemon cannot guarantee cleanup. Its ping
  watchdog provides a best-effort recovery only while the daemon still runs.
- The preference takes effect the next time the wake Activity appears.

Reference: https://github.com/Benjamin-Wiegand/Flywheel/issues/3
Scrcpy principle: https://github.com/Genymobile/scrcpy/blob/master/doc/device.md
