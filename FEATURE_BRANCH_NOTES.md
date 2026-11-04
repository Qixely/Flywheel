# Waze startup - standalone experimental branch

Uses the stock upstream UI, with an OFF-by-default switch to open Waze once
per projection after unlock, package list and privileged launcher are ready.
Does not auto-open if user has selected an app.

The integrated local version was tested, where this option defaulted ON.
This isolated branch intentionally defaults OFF to avoid upstream UX surprise.
Future upstream version should support a configurable app, not only Waze.
Not independently compiled or device-tested here.
