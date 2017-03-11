# About Game
Just some random game without a name I feel like making. That's right, in one class file ;)

# Installing Game
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
sh run.sh pan=false jump=true 8bit=true
```

The allowed parameters are:
- `jump` - Enable jumping? Can be `true` or `false`
- `pan` - Enable panning? Can be `true` or `false`
- `8bit` - Force 8-bit colors? Can be `true` or `false`

# Controls
- `W` - Move up
- `A` - Move left
- `S` - Move down
- `D` - Move right
- `R` - Respawn
- `B` - Toggle 8-bit rendering mode
- `<SPACE>` - Jump (disabled by default)
- `<RIGHT_CLICK>` - Hold to pan view and speed walk
- `<MIDDLE_CLICK>` - Hold to speed walk towards mouse
- `<SHIFT>` - Slow walk
