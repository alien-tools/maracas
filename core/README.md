# Notes

- We currently check for source-incompatible detections, not binary-incompatibles ones, so they're not detected yet. A `fieldNowStatic` used through a super keyword, for instance, isn't reported.
- We now have access to generics; write tests + detections for them

