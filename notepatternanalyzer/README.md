# Note Pattern Analyzer

## Completed
* Midi text parsing. We used http://flashmusicgames.com/cgi-sys/suspendedpage.cgi to do the conversions.
* Note relative distance calculation <- Completed?

## In progress
* Chord guessing via HMM

## Planned
* Training probalities for chord guessing
* Parsing output back into analyzer

## Possible? (just some ideas on what can maybe be done)
* Parse midi files directly
* Find and isolate common patterns in music.
* Separate track into separate hands (sibelius midis don't seem to do this by default)
* Improve note length detection (particularly for musescore output)
	* Implement time snapping so midis straight from keyboard can be analyzed
* Recommend fingerings