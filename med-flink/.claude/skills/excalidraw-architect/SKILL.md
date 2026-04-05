---
name: excalidraw-architect
description: Use this skill when the user asks to draw, diagram, visualize, or sketch an architecture, pipeline, flow, or system as an Excalidraw diagram. Trigger phrases include "draw a diagram", "create an excalidraw", "visualize the architecture", "sketch the pipeline", "generate a diagram".
version: 0.1.0
---

# Excalidraw Architect

When this skill is invoked, generate an Excalidraw diagram based on the user's description and write it to a `.excalidraw` file.

## Output format

Write a valid Excalidraw JSON file (`.excalidraw` extension). The schema:

```json
{
  "type": "excalidraw",
  "version": 2,
  "source": "https://excalidraw.com",
  "elements": [ ...elements... ],
  "appState": {
    "gridSize": null,
    "viewBackgroundColor": "#ffffff"
  },
  "files": {}
}
```

## Element reference

### Rectangle
```json
{
  "id": "<unique-id>",
  "type": "rectangle",
  "x": 100, "y": 100, "width": 180, "height": 80,
  "angle": 0,
  "strokeColor": "#1e1e1e",
  "backgroundColor": "#a5d8ff",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 1,
  "opacity": 100,
  "groupIds": [],
  "roundness": { "type": 3 },
  "seed": 1,
  "version": 1,
  "versionNonce": 1,
  "isDeleted": false,
  "boundElements": [],
  "updated": 1,
  "link": null,
  "locked": false
}
```

### Text (standalone label)
```json
{
  "id": "<unique-id>",
  "type": "text",
  "x": 110, "y": 130,
  "width": 160, "height": 25,
  "angle": 0,
  "strokeColor": "#1e1e1e",
  "backgroundColor": "transparent",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 1,
  "opacity": 100,
  "groupIds": [],
  "roundness": null,
  "seed": 1,
  "version": 1,
  "versionNonce": 1,
  "isDeleted": false,
  "boundElements": [],
  "updated": 1,
  "link": null,
  "locked": false,
  "text": "My Label",
  "fontSize": 16,
  "fontFamily": 1,
  "textAlign": "center",
  "verticalAlign": "middle",
  "baseline": 14,
  "containerId": null,
  "originalText": "My Label"
}
```

### Arrow
```json
{
  "id": "<unique-id>",
  "type": "arrow",
  "x": 280, "y": 140,
  "width": 80, "height": 0,
  "angle": 0,
  "strokeColor": "#1e1e1e",
  "backgroundColor": "transparent",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 1,
  "opacity": 100,
  "groupIds": [],
  "roundness": { "type": 2 },
  "seed": 1,
  "version": 1,
  "versionNonce": 1,
  "isDeleted": false,
  "boundElements": [],
  "updated": 1,
  "link": null,
  "locked": false,
  "points": [[0, 0], [80, 0]],
  "lastCommittedPoint": null,
  "startBinding": {
    "elementId": "<source-rect-id>",
    "focus": 0,
    "gap": 2
  },
  "endBinding": {
    "elementId": "<target-rect-id>",
    "focus": 0,
    "gap": 2
  },
  "startArrowhead": null,
  "endArrowhead": "arrow"
}
```

### Diamond
Same as rectangle but `"type": "diamond"`.

### Ellipse
Same as rectangle but `"type": "ellipse"`.

## Layout guidelines

- Space boxes **200–240px apart horizontally**, **160px vertically**
- Left-to-right flow for pipelines; top-to-bottom for layered architectures
- Use **grouped rectangles** (same `groupIds` array value) to represent subsystems
- Use a large light-grey rectangle (`backgroundColor: "#f8f9fa"`, no stroke: `strokeColor: "transparent"`) behind a group as a container/swimlane
- Color palette suggestions:
  - External systems: `#ffd8a8` (orange-tint)
  - Processing / compute: `#a5d8ff` (blue-tint)
  - Storage / databases: `#b2f2bb` (green-tint)
  - Queues / streams: `#e9ecef` (grey)
  - Alerts / outputs: `#ffa8a8` (red-tint)
- Arrow labels: add a `text` element with `containerId` set to the arrow's id and `verticalAlign: "middle"` for inline labels

## IDs

Generate short, deterministic, human-readable IDs (e.g. `"kafka-source"`, `"anomaly-fn"`, `"alert-sink"`). Do **not** use random UUIDs — readable IDs make the JSON easier to edit.

## Steps to follow

1. Read the user's description and identify: components, data flows, groupings, and labels.
2. Plan a grid layout (calculate x/y coordinates before writing JSON).
3. Write the `.excalidraw` file using the Write tool.
4. Tell the user the filename and how to open it: drag-and-drop onto [excalidraw.com](https://excalidraw.com) or open with the Excalidraw VS Code extension.
