package ru.vk.recommender.sre.discoveryportalflow.service.servicehost.model

private const val SERVICE_LINE = "- service:"

object MeshServiceUpdater {

    /**
     * Adds a new service to the upstreams section in the provided file content
     *
     * @param serviceName Name of the service to add
     * @param fileContent Original file content in YAML format
     * @return Updated file content with the new service added
     */
    fun addServiceToUpstreams(serviceName: String, fileContent: String): String {
        val lines = fileContent.split("\n")
        val resultLines = mutableListOf<String>()
        var i = 0

        while (i < lines.size) {
            resultLines.add(lines[i])

            // Check if current line contains "upstreams:"
            if (lines[i].trimStart().startsWith("upstreams:")) {
                // Find the indentation of the "upstreams:" line
                val upstreamsIndentation = lines[i].indexOfFirst { !it.isWhitespace() }

                // Calculate the indentation for the new service entry (same as upstreams indentation)
                val serviceIndentation = " ".repeat(lines[i + 1].indexOfFirst { !it.isWhitespace() })

                // Check if the service is not already in the upstreams list
                var alreadyExists = false
                var j = i + 1

                while (j < lines.size) {
                    val currentLine = lines[j]
                    val currentIndentation =
                        if (currentLine.isBlank()) Int.MAX_VALUE else currentLine.indexOfFirst { !it.isWhitespace() }

                    // If we reach a line with same or less indentation than "upstreams:" and it's not a list item,
                    // we've moved out of the upstreams block
                    if (currentIndentation < upstreamsIndentation && currentLine.isNotBlank()) {
                        break
                    } else if (currentIndentation == upstreamsIndentation && currentLine.isNotBlank()) {
                        // If the indentation is the same, but it's not a list item (- ), we've moved out of the upstreams block
                        if (!currentLine.trimStart().startsWith(SERVICE_LINE)) {
                            break
                        }
                    }

                    // Check if this line contains our service
                    if (currentLine.trimStart().startsWith("$SERVICE_LINE $serviceName")) {
                        alreadyExists = true
                        break  // Exit loop early once we find the service
                    }

                    j++
                }

                // Add the new service entry on the next line if it doesn't already exist
                if (!alreadyExists) {
                    resultLines.add("${serviceIndentation}$SERVICE_LINE \"$serviceName\"")
                }
            }

            i++
        }

        return resultLines.joinToString("\n")
    }
}
