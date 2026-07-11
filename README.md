# Quarterchunk Unit Simple

This tool is a simple GUI editor that helps users build Quarterchunk Unit buildings more easily in Minecraft.

Users can draw over a multi-layer pixel editor to design the overall layout of QC Units. Users can also draw inner walls inside the buildings using `shift` key.

Users can then configure block choices and upload customized .nbt files to determine the style of each QC Unit:

- Framework Block: The edges of a QC Unit.
- Row Block: The row attached to the framework on the outside.
- Column Block: The column attached to the framework on the outside.
- Wall Block: The default wall used above the ceiling and below the floor.
- Floor Block: The floor.
- Inner Wall: 7x7x1 / 7x7x3 sized .nbt structure file. The uploaded file will be identified as the north wall.
- Outer Wall: Same as inner wall but can be 7x7x(1~3) where the extra layer will be on the outside.
- Inner Column: A 3x7x3 stylistic column when 2 by 2 Units join together without inner walls.
- Gate: Another 7x7x1 / 7x7x3 inner wall that allows passage.

### Sample Interface

<img width="1090" height="777" alt="Screenshot_20260701_230150" src="https://github.com/user-attachments/assets/22a4aaee-9e69-4fb6-bdcd-4ee01e4520de" />

### Sample Building

<img width="1920" height="1080" alt="2026-07-01_18 13 06" src="https://github.com/user-attachments/assets/85d0ef3c-9216-43e5-9133-29a023569f09" />

## Quarterchunk Units

**Quarterchunk (QC) Units** are 9x9x9 cubes that share sides with adjacent Units. They usually have 7x7x7 interior space.

QC Units can be a good choice for building cities or other buildings useful for survival mode.
