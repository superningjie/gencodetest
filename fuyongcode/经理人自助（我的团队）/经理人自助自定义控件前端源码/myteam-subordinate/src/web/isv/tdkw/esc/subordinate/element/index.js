import { Select, Option, Radio, RadioGroup, Button } from "element-ui";

const coms = [Select, Option, Radio, RadioGroup, Button];

export default {
  install(Vue, options) {
    coms.map((c) => {
      Vue.component(c.name, c);
    });
  },
};
