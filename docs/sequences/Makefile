DIAGRAMS_SOURCES = $(wildcard *.txt)
DIAGRAMS = $(DIAGRAMS_SOURCES:.txt=.png)
all: $(DIAGRAMS)

%.png: %.txt
	./generate_sequence_diagram.py $^ $@
