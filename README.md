<!-- Qixely-cellular-fix-branch -->
> [!NOTE]
> **Qixely focused fix: cellular signal levels above the icon range**
>
> **Problem:** On a stock BOOX Palma 2 Pro, a reported level of **5** caused an `AssertionError` in `CellIndicatorIcon.update()`, terminating Flywheel and dropping Android Auto projection.
>
> **Fix:** Remove the range assertion before the existing `Math.clamp(bars, 0, CELL_BARS)` call. The icon still shows at most **4 bars**; unexpected levels no longer crash the UI.
>
> **[Reproduction, source change, and test notes](CELL_SIGNAL_FIX.md)**. This focused branch contains the general compatibility fix, not the Ford/Spotify/DPI modifications from [`palma2pro-working`](https://github.com/Qixely/Flywheel/tree/palma2pro-working).

---

# Flywheel
> [!caution]
> - Flywheel is experimental software.
> - Use it at your own risk.
> - I am not responsible for bricked headunits.

Flywheel is a free and open source alternative to Android Auto intended for users of de-googled phones, aiming to be compatible with existing headunits.

![picture of it running](https://ben.wiegand.pw/img/flywheel-dev-emulated-thumb.webp)


## Documentation

For user documentation, visit the [wiki tab](https://github.com/Benjamin-Wiegand/Flywheel/wiki).

#### Quick links
- [Features](https://github.com/Benjamin-Wiegand/Flywheel/wiki#features)
- [Setup guide](https://github.com/Benjamin-Wiegand/Flywheel/wiki/Setup-Guide)
- [Certificates and Keys](https://github.com/Benjamin-Wiegand/Flywheel/wiki/Certificates-and-Keys)
- [Headunit compatibility](https://github.com/Benjamin-Wiegand/Flywheel/wiki/Headunit-Compatibility)
- [Troubleshooting](https://github.com/Benjamin-Wiegand/Flywheel/wiki/Troubleshooting)


## Bug reporting

If you encounter any issues, please [visit the troubleshooting wiki page](https://github.com/Benjamin-Wiegand/Flywheel/wiki/Troubleshooting) first. If you can't resolve the issue, please [submit a bug report](https://github.com/Benjamin-Wiegand/Flywheel/issues/new/choose).


## Contributing

Feel free to [look for or open a new issue](https://github.com/Benjamin-Wiegand/Flywheel/issues) for something you want to work on, and say that you'll work on it. Other contributors may provide helpful information or warn you of upcoming churn, so please pay attention to new comments on the issue. 

Please note that contributions may not always be accepted, but are always appreciated!


## Thanks

Non-code contributions:

- [Nick](https://github.com/4channel) for helping test on his car during early development
- [Zelemar Electronics](https://zelemar.eu) for helping me diagnose an OEM headunit for bench testing

Other projects of great help during development:

- [aasdk](https://github.com/f1xpl/aasdk/)
- [OpenAuto](https://github.com/f1xpl/OpenAuto/)
- [scrcpy](https://github.com/genymobile/scrcpy)

