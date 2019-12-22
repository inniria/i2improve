# I2Improve
## Improved Improved Improve mod for Wurm

This mod is a total re-write of the [Improved Improve](https://github.com/munsta0/WUClientImprovedImprove) mod by Munsta0, with additional features added. It is an improve mod that automatically chooses the right tool and action to improve an item, working both on items inside containers and items on the world.

## How to install
Firstly, it requires [Ago's client modloader](https://github.com/ago1024/WurmClientModLauncher/releases/). You can then download the file i2improve.zip and extract its contents inside the **WurmLauncher/mods** folder.

## How does it work

In order for the mod to work, you first have to bind a key to it. You can do that with the following console command:

**/bind \<key\> "i2improve"**

For instance, to bind key X, use the following command:

**/bind X "i2improve"**

Once the key is bound, you must add the improvement tools to the toolbelt. The mod will automatically choose the right tool from it during improvement.

### Improving items inside a container

Just hover over the item and press the key. The right tool will be selected and the item will be improved with it. If you choose a stack of items (eg. a group of barrels), the one with the lowest quality will be improved. In case of the item being damaged, it will be repaired automatically before trying to improve it.

### Improving items from the world

When improving an item that is on the ground, you must first double click the item in order to get the item description. After that, the mod works as usual: just press the key bind to improve and repair as needed.
