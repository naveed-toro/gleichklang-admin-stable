var bbApp = {
    init: function() {
        var sepaDecider = document.getElementById("sepaSwitch");

        var sepaSelect = sepaDecider.querySelector("#sepaSwitch");
        sepaSelect.style.display = "none";

        var parentElement = sepaSelect.parentNode;

        var checkBoxElementSepa = this.createRadioBox("newSepaDecider", "IBAN", "choice-1");
        var checkBoxElementTraditional = this.createRadioBox("newSepaDecider", "Kontonummer & Bankleitzahl", "choice-2");

        var labelSepa = this.createLabel("IBAN", "choice-1");
        var labelTraditional = this.createLabel("Kontonummer & Bankleitzahl", "choice-2");

        var wrapper = document.createElement("div");
        wrapper.setAttribute("id", "bbSEPADecider");


        wrapper.appendChild(checkBoxElementSepa);
        wrapper.appendChild(labelSepa);
        wrapper.appendChild(checkBoxElementTraditional);
        wrapper.appendChild(labelTraditional);

        parentElement.appendChild(wrapper);

        checkBoxElementSepa.addEventListener("change", function() {
            sepaSelect.selectedIndex = 1;
            changeSepaSwitch(sepaSelect, 'directDebit');
        });
        checkBoxElementTraditional.addEventListener("change", function() {
            sepaSelect.selectedIndex = 2;
            changeSepaSwitch(sepaSelect, 'directDebit');
        });

        checkBoxElementSepa.checked = true;
        sepaSelect.selectedIndex = 1;
        changeSepaSwitch(sepaSelect, 'directDebit');
    },


    createLabel: function(name, element) {
        var label = document.createElement("label");
        label.appendChild(document.createTextNode(name));
        label.setAttribute("for", element);

        return label;
    },

    createRadioBox: function(name, value, elementId) {
        var radioBox = document.createElement("input");
        radioBox.setAttribute("type", "radio");
        radioBox.setAttribute("name", name);
        radioBox.setAttribute("value", value);
        radioBox.setAttribute("id", elementId);

        return radioBox;
    },

};


/*
 * In original implementation, optional fields have stars.
 * We use the opposite.
 */
document.addEventListener('DOMContentLoaded', function() {
    var labels = document.getElementsByClassName('label');
    for (var i = 0; i < labels.length; i++) {
        if (labels[i].textContent.indexOf('*') > 0) {
            labels[i].textContent = labels[i].textContent.replace(' *', '');
        } else {
            labels[i].firstElementChild.className += ' form-required';
            for (var j = 1; j < labels[i].childNodes.length; j++) {
                labels[i].childNodes[j].textContent = '';
            }
        }
    }
});

document.addEventListener('DOMContentLoaded', function () {
    bbApp.init();
});
