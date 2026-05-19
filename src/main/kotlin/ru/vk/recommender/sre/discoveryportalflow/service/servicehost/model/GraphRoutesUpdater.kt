package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model

object GraphRoutesUpdater {

    /**
     * Adds a new route to the routes section in the provided file content
     *
     * @param graphName Name of the graph to add as a route
     * @param fileContent Original file content in YAML format
     * @return Updated file content with the new route added
     */
    fun addRoute(graphName: String, fileContent: String): String {
        val lines = fileContent.split("\n")
        val resultLines = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            resultLines.add(lines[i])

            // Check if current line contains "routes:"
            if (lines[i].trimStart().startsWith("routes:")) {
                // Find the indentation of the "routes:" line
                val routesIndentation = lines[i].indexOfFirst { !it.isWhitespace() }

                // Calculate the indentation for the new route entry (routes indentation for list items)
                val routeIndentation = " ".repeat(routesIndentation)

                // Check if the route is not already in the routes list
                var alreadyExists = false
                var j = i + 1

                while (j < lines.size) {
                    val currentLine = lines[j]
                    val currentIndentation = if (currentLine.isBlank()) Int.MAX_VALUE else currentLine.indexOfFirst { !it.isWhitespace() }

                    // If we reach a line with same or less indentation than "routes:" and it's not a list item,
                    // we've moved out of the routes block
                    if (currentIndentation < routesIndentation && currentLine.isNotBlank()) {
                        break
                    } else if (currentIndentation == routesIndentation && currentLine.isNotBlank()) {
                        // If the indentation is the same, but it's not a list item (- ), we've moved out of the routes block
                        if (!currentLine.trimStart().startsWith("- ")) {
                            break
                        }
                    }

                    // Check if this line contains our route path
                    if (currentLine.trimStart().startsWith("- path: /api/v1/$graphName")) {
                        alreadyExists = true
                        break  // Exit loop early once we find the route
                    }

                    j++
                }

                // Add the new route entry on the next line if it doesn't already exist
                if (!alreadyExists) {
                    resultLines.add("${routeIndentation}- path: /api/v1/$graphName")
                    resultLines.add("${routeIndentation}  graph: $graphName")
                    resultLines.add("${routeIndentation}  prefix: true")
                }
            }

            i++
        }

        return resultLines.joinToString("\n")
    }
}
