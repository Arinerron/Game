# About Game
Just some random game without a name I feel like making. That's right, in one class file ;)

# Installing Game
```
trizen -S game-git
```

or

```
git clone https://github.com/Arinerron/Game.git && cd Game
```

# Starting Game
To start the game, simply execute:
```
sh run.sh
```

Or, you can optionally add space-separated parameters, like:
```
sh run.sh --nopan --jump --dither
```

The allowed parameters are:
- `-d/--dither` - Force 8bit colors & enable dithering?
- `-p/--nopan` - Disable panning?
- `-j/--jump` - Enable jumping?
- `-e/--tileeditor` - Launch tile editor instead of game?

# Controls
- `W` - Move up
- `A` - Move left
- `S` - Move down
- `D` - Move right
- `R` - Respawn
- `B` - Toggle 8-bit rendering mode
- `F2` - Screenshot
- `F3` - Display debug info
- `<SPACE>` - Jump (disabled by default)
- `<RIGHT_CLICK>` - Hold to pan view and speed walk
- `<MIDDLE_CLICK>` - Hold to speed walk towards mouse
- `<SHIFT>` - Slow walk

# Tile Editor
```
sh run.sh --tileeditor
```
