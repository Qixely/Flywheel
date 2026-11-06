# Fix: cellular signal-level assertion crashes projection

**Branch:** `fix/clamp-cellular-signal-level`  
**Scope:** One production Java file; unrelated projection, head-unit, display, and media code is unchanged.  
**Observed:** November 5, 2026, BOOX Palma 2 Pro (stock, ShizukuPlus), while projecting to Ford SYNC 4.

## Symptom and evidence

Flywheel displayed Android Auto normally, then abruptly exited. The vehicle reported a USB connection error as a consequence of the application process terminating. The app's debug recording stopped at the exception.

The relevant sequence from `20261105-143735.log.txt`:

```text
14:38:33.098  ControlChannel: ping! PingRequest[...]
14:38:34.005  CellIndicator: ... bars=5 ...
14:38:34.007  AndroidRuntime: FATAL EXCEPTION: main
14:38:34.007  AndroidRuntime: java.lang.AssertionError
14:38:34.007      at ...indicator.CellIndicatorIcon.update(CellIndicatorIcon.java:30)
```

This is one *confirmed* reason for a session failure. It does not prove that every USB disconnect or audio-recovery problem has the same cause.

## Root cause

`CellIndicatorIcon.CELL_BARS` is **4**, but the phone delivered **5** through its telephony signal callback. Previously, the indicator method asserted that the input was between 0 and 4 **before** applying a clamp. With assertions active, a signal level of 5 terminated the Android main thread.

Previous logic:

```java
assert bars >= 0 && bars <= CELL_BARS;
bars = Math.clamp(bars, 0, CELL_BARS);
```

Updated logic:

```java
// Some Android implementations report more levels than this icon supports.
bars = Math.clamp(bars, 0, CELL_BARS);
```

The existing four-bar graphic, drawable level handling, and everything else in `CellIndicatorIcon.update()` are preserved.

## Expected behavior

| Incoming value | Displayed bars |
| ---: | ---: |
| -1 | 0 |
| 0 | 0 |
| 1–4 | Unchanged |
| 5 | 4 |
| 6 or higher | 4 |

No crash should occur solely because an out-of-range signal value is received.

## Verification guidance

1. Confirm the assertion is absent and the existing clamp is present before `cellLevels.setLevel(bars)`.
2. Feed boundary values -1, 0, 4, 5, and 6 through the indicator; verify that no assertion is thrown and drawable levels are clamped.
3. During normal real-world signal changes, keep debug logging enabled and confirm a five-bar update does not terminate projection.
4. Track unrelated USB, encoder, and audio-focus failures separately. Extended-session stability has not been fully established.

**No pull request is created by this branch or by the personal sync kit.** This branch exists so the maintainer can inspect a small isolated fix; `palma2pro-working` combines it with the personal modifications.
