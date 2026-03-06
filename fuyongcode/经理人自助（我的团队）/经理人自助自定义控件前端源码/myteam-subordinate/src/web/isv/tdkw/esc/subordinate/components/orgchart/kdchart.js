import * as d3 from "./d3";
import {
  FONT_HEIGHT_MAP,
  SVG_FONT_FAMILY,
  CHART_WIDTH_S12FN,
  CHART_WIDTH_S14FN,
  CHART_WIDTH_S16FN,
  CHART_WIDTH_S12F6,
  CHART_WIDTH_S14F6,
  CHART_WIDTH_S16F6,
  deepClone,
} from "../../utils/config";
const ViewOffset = 1000;

export default class KDChart {
  constructor({
    dom = document.body,
    data = {},
    cardModel = {},
    cardConfig = {},
    getCardStyle = () => {},
    getCollapseStyle = () => {},
    toCardDetail = () => {},
  }) {
    this.dom = dom;
    this.data = deepClone(data);
    this.cardModel = cardModel;
    this.cardConfig = cardConfig;
    this.getCardStyle = getCardStyle;
    this.getCollapseStyle = getCollapseStyle;
    this.toCardDetail = toCardDetail;
    this.foldObj = {};
    this.paintST = null;
    this.paintInfoTime = 0;
    this.currMovementX = 0;
    this.currMovementY = 0;
    this.init();
  }

  init() {
    this.initStatus();
    this.initParams();
    // this.limitDataLevel(this.data);
    this.initChartData();
    this.initDom();
    this.initChart();
  }

  initStatus() {
    this.svgSize = {
      width: 0,
      height: 0,
      rx: 0,
      ry: 0,
    };

    this.groupLoc = {
      x: 0,
      y: 0,
      k: 1,
    };

    this.relativeMaxLoc = {
      x: -Infinity,
      y: -Infinity,
    };
  }

  initParams() {
    this.theme = {};
    this.cardStyle = {
      baseCardSize: this.cardConfig.baseCardSize,
    };
    this.svgWrapperSize = {
      width: this.dom.offsetWidth,
      height: this.dom.offsetHeight,
    };
    this.iconConfig = {
      toggleMinus: {
        unicode: "-",
        color: "#fff",
        x: 102.2,
        y: 93,
      },
      togglePlus: {
        unicode: "+",
        color: "#fff",
        x: 100,
        y: 94.4,
      },
      class: "iconfont",
      fontSize: 17,
    };
  }

  initChartData() {
    const _this = this;
    const expandLevel = this.cardConfig.expandLevel;
    const locMap = this.getLocations(true);
    const getNodes = this.descendants;
    const getLinks = this.links;

    function eachData(list, parent = null, level = 0) {
      let children = [];
      for (let i = 0; i < list.length; i++) {
        let treeItem = {
          ...list[i],
          ...locMap[list[i].id + list[i].positionid],
          id: list[i].id,
          data: list[i],
          cardModel: list[i].cardModel,
          level,
          parent,
          getNodes,
          getLinks,
        };
        treeItem.x0 = treeItem.x;
        treeItem.y0 = treeItem.y;

        if (list[i].children && list[i].children.length > 0) {
          const children = eachData(list[i].children, treeItem, level + 1);

          // 设置expandLevel时折叠
          if (level >= expandLevel - 1) {
            treeItem.children = null;
            _this.foldObj[treeItem.id + treeItem.positionid + ""] = true;
          } else {
            treeItem.children = children;
          }
          treeItem._children = children;
        }
        children.push(treeItem);
      }
      return children;
    }

    this.data = eachData([this.data], null, 0)[0];
  }

  limitDataLevel(treeData) {
    const limitLevel = this.cardConfig.limitLevel;
    function eachTreeData(citem, level) {
      if (level === limitLevel) {
        citem._children = citem.children;
        citem.children = null;
      }
      if (citem.children && citem.children.length > 0) {
        citem.children.map((child) => {
          eachTreeData(child, level + 1);
        });
      }
    }
    eachTreeData(treeData, 1);
  }

  getLocations = (isInit = false) => {
    let locMap = {};
    const {
      baseCardSize: { width, height, xSpace, ySpace },
    } = this.cardConfig;
    let mateData = deepClone(this.data);
    console.log(mateData, "mateData");
    if (isInit) {
      this.expandDataLevel(mateData);
    } else {
      this.foldDataLevel(mateData);
    }

    let tree = d3.tree().nodeSize([width + xSpace, height + ySpace]);
    let root = d3.hierarchy(mateData);

    tree(root);
    root.descendants().forEach((node) => {
      locMap[node.data.id + node.data.positionid] = {
        x: node["x"],
        y: node["y"] + this.cardConfig.baseCardSize.height / 2 + 30,
        depth: node.depth,
      };
    });
    return locMap;
  };

  expandDataLevel(mateData) {
    const expandLevel = this.cardConfig.expandLevel;
    function each(list, level) {
      for (let i = 0; i < list.length; i++) {
        if (list[i].children) {
          if (level < expandLevel) {
            each(list[i].children, level + 1);
          } else {
            list[i].children = null;
          }
        }
      }
    }
    each([mateData], 1);
  }

  foldDataLevel(mateData) {
    const _this = this;
    this.data.getNodes().map((item) => {
      if (
        item.children &&
        item.children.length > 0 &&
        this.foldObj[item.id + item.positionid]
      ) {
        this.foldObj[item.id + item.positionid] = false; // foldObj为true表示该节点要被折叠，false表示该节点要被展开
      }
    });
    function each(list) {
      for (let i = 0; i < list.length; i++) {
        if (list[i].children) {
          if (_this.foldObj[list[i].id + list[i].positionid]) {
            // 要被折叠，孩子节点置为null
            list[i].children = null;
          } else {
            each(list[i].children);
          }
        }
      }
    }
    each([mateData]);
  }

  descendants() {
    let res = [];
    each([this]);
    function each(list) {
      for (let i = 0; i < list.length; i++) {
        const item = list[i];
        res.push(item);
        if (item.children && item.children.length > 0) {
          each(item.children);
        }
      }
    }
    return res;
  }

  links() {
    let res = [];
    each([this]);
    function each(list) {
      for (let i = 0; i < list.length; i++) {
        const item = list[i];
        if (item.children && item.children.length > 0) {
          item.children.map((child) => {
            res.push({
              source: item,
              target: child,
            });
          });
          each(item.children);
        }
      }
    }
    return res;
  }

  initDom() {
    this.svgwrapperID = this.dom.getAttribute("id") + "" + "svg_wrapper";
    if (d3.select("#" + this.svgwrapperID))
      d3.select("#" + this.svgwrapperID).remove();
    this.svgWrapper = d3
      .create("div")
      .attr("class", "svg_wrapper")
      .attr("id", this.svgwrapperID)
      .style("width", this.svgWrapperSize.width + "px")
      .style("height", this.svgWrapperSize.height + "px")
      .style("background", "#fff")
      .style("box-sizing", "border-box")
      .style("position", "absolute")
      .style("overflow", "hidden")
      .style("left", "50%")
      .style("top", "0")
      .style("transform", "translateX(-50%)");
    this.dom.appendChild(this.svgWrapper.node());
  }

  initChart() {
    if (!this.data) return;
    this.svgWrapper.html(null);
    this.svgWrapper.append(() => this.initSvg());
  }

  initSvg() {
    this.svg = d3
      .create("svg")
      .style("background", "#fff")
      .attr("style", "position:relative;user-select:none")
      .style("width", this.svgWrapperSize.width + "px")
      .style("height", this.svgWrapperSize.height + "px")
      .style("transform", `translate(0px, 0px)`);

    this.group = this.svg.append("g");
    this.gLink = this.group
      .append("g")
      .attr("id", "pathGroup")
      .attr("fill", "none");
    this.gNode = this.group
      .append("g")
      .attr("id", "nodeGroup")
      .attr("cursor", "pointer")
      .attr("pointer-event", "visible");
    this.bindSvgEvent();
    this.readyPaint();
    return this.svg.node();
  }

  bindSvgEvent() {
    this.svg.call(
      d3.zoom().on("zoom", (e) => {
        switch (e.sourceEvent.type) {
          case "wheel":
            let step = 0.2;
            let k =
              this.groupLoc.k + (e.sourceEvent.deltaY > 0 ? -1 : 1) * step;
            k = k >= 2 ? 2 : k;
            k = k <= 0.5 ? 0.5 : k;

            this.resetGroupLoc("move", {
              rx: 0,
              ry: 0,
              k,
            });
            break;
          case "mousemove":
            this.resetGroupLoc("move", {
              rx: e.sourceEvent.movementX,
              ry: e.sourceEvent.movementY,
              k: this.groupLoc.k,
            });
            break;
        }
      })
    );
  }

  readyPaint() {
    this.beforePaint();
    this.mainPaint();
  }

  beforePaint(moveNodeOffset = { rx: 0, ry: 0 }, type = "move") {
    this.extremeNodeLoc();
    if (this.isRelativeMaxChange) {
      this.getSvgGroup2Loc(moveNodeOffset);
      this.resetSvgLoc();
      this.resetGroupLoc(type);
    }
  }

  getSvgGroup2Loc(moveNodeOffset) {
    // 计算svg所需的宽度和高度
    const width = Math.max(
      this.relativeMaxLoc.x * 2 + this.cardConfig.baseCardSize.width * 2,
      this.svgWrapperSize.width
    );
    const height = Math.max(
      this.relativeMaxLoc.y + 150,
      this.svgWrapperSize.height
    );

    // svg的宽高和位置
    this.svgSize = {
      width,
      height,
      rx: (this.svgWrapperSize.width - width) / 2, // 偏离后，居中
      ry: 0,
    };

    let k = 1;
    if (this.groupLoc && this.groupLoc.k) {
      k = this.groupLoc.k;
    }

    this.groupLoc = {
      x: this.svgSize.width / 2 - this.data.x + moveNodeOffset.rx,
      y: moveNodeOffset.ry,
      k,
    };

    this.intialGroupLoc = {
      x: this.svgSize.width / 2 - this.data.x, // 水平始终居中
      y: 0,
      k,
    };
  }

  resetSvgLoc() {
    this.svg
      .style("width", this.svgSize.width + "px")
      .style("height", this.svgSize.height + "px")
      .style(
        "transform",
        `translate(${this.svgSize.rx}px,${this.svgSize.ry}px)`
      );
  }

  resetGroupLoc(
    type = "move",
    loc = {
      rx: 0,
      ry: 0,
      k: this.groupLoc.k,
    }
  ) {
    this.groupLoc.x += loc.rx;
    this.groupLoc.y += loc.ry;
    this.groupLoc.k = loc.k || this.groupLoc.k;
    this.group.attr(
      "transform",
      "translate(" +
        this.groupLoc.x +
        "," +
        this.groupLoc.y +
        ") scale(" +
        this.groupLoc.k +
        ")"
    );

    // 如果type为fold，表示折叠展开节点，则不考虑在X轴或Y轴移动的像素，直接绘制卡片和连线
    if (type === "fold") {
      console.log("进来了。。。");
      this.prePaintInfo();
    } else {
      // 缩放、移动操作需要更新加载的节点(卡片节点按需加载)
      this.lazyPaintRest(loc.rx, loc.ry);
    }
  }

  // 判断svg在X轴或者Y轴移动的像素，是否达到了更新视图的值，需累计移动ViewOffset（1000）像素
  lazyPaintRest(rx, ry) {
    this.currMovementX += rx;
    this.currMovementY += ry;

    if (
      Math.abs(this.currMovementX) >= ViewOffset * this.groupLoc.k ||
      Math.abs(this.currMovementY) >= ViewOffset * this.groupLoc.k
    ) {
      this.currMovementX = 0;
      this.currMovementY = 0;
      this.prePaintInfo();
    }
  }

  //绘制svg里面的内容（卡片、连线）
  prePaintInfo() {
    if (this.paintST) {
      clearTimeout(this.paintST);
    }

    if (new Date().getTime() - this.paintInfoTime > 500) {
      this.paintInfoTime = new Date().getTime();
      this.resetCardAndPath();
    } else {
      this.paintST = setTimeout(() => {
        this.resetCardAndPath();
      }, 0);
    }
  }

  // 动态更新卡片、连线
  resetCardAndPath() {
    this.paintCardInfo();
    this.partPathPaint();
  }

  partPathPaint() {
    this.linkUpdate._groups[0].map((path) => {
      let flag =
        !this.isInPaintView(path.__data__.target) &&
        !this.isInPaintView(path.__data__.source);
      path.setAttribute("d", this.elbow(flag ? "" : path.__data__));
    });
  }

  isInPaintView(node) {
    if (!node) return false;

    let groupPot = {
      x: this.svgSize.width / 2,
      y: 0,
    };
    let isin = true;
    let minX = -this.svgWrapperSize.width * 0.5 - ViewOffset;
    let maxX = this.svgWrapperSize.width * 0.5 + ViewOffset;
    let minY = -this.svgWrapperSize.height * 0 - ViewOffset;
    let maxY = this.svgWrapperSize.height * 1 + ViewOffset;

    // (this.groupLoc.x - groupPot.x)  group横向偏移距离
    // flagX 该卡片横向偏移后的坐标
    let flagX = node.x * this.groupLoc.k + (this.groupLoc.x - groupPot.x);
    // (this.groupLoc.y - groupPot.y)  group纵向偏移距离
    // flagY该卡片纵向偏移后的坐标
    let flagY = node.y * this.groupLoc.k + (this.groupLoc.y - groupPot.y);

    // 偏移后的相对坐标，在可加载区域内，则渲染
    isin = flagX > minX && flagX < maxX && flagY > minY && flagY < maxY;
    return isin;
  }

  resetPaint(curNode, fold = false, type = "move") {
    this.beforePaint(
      {
        rx: this.groupLoc.x - this.intialGroupLoc.x,
        ry: this.groupLoc.y - this.intialGroupLoc.y,
      },
      type
    );
    this.paintCard(curNode, fold);
    this.paintPath(curNode, fold);
  }

  resetChartData() {
    const locMap = this.getLocations();

    this.data.getNodes().forEach((node) => {
      let newLoc = locMap[node.id + node.positionid];
      node.x = newLoc.x;
      node.y = newLoc.y;
      node.depth = newLoc.depth;
    });
  }

  mainPaint() {
    this.paintPath(this.data);
    this.paintCard(this.data);
  }

  paintPath(curNode, foldFlag) {
    this.paths = this.data.getLinks();
    let transition = this.group.transition().duration(500);
    const linkGroup = this.gLink
      .selectAll("path")
      .data(this.paths, (d) => d["target"]["id"] + d["target"]["positionid"]);
    const linkEnter = linkGroup.enter().append("path");
    linkGroup
      .exit()
      .remove()
      .attr("d", this.endElbow(curNode))
      .attr("stroke-opacity", 0);

    if (!foldFlag) {
      this.paintPathInfo(linkEnter, curNode);
    }

    this.linkUpdate = linkGroup
      .merge(linkEnter)
      .transition(transition)
      .attr("stroke-opacity", 1)
      .attr("d", (d) => this.elbow(d))
      .attr("stroke", "#69deff");
  }

  paintPathInfo(linkEnter, curNode) {
    linkEnter
      .attr("cursor", "move")
      .attr("stroke", "#69deff")
      .attr("stroke-opacity", 0.1)
      .attr("stroke-width", 1)
      .attr("shape-rendering", "auto")
      .attr("d", this.initElbow(curNode));
  }

  elbow(path) {
    if (!this.isInPaintView(path.target) && !this.isInPaintView(path.source))
      return "";
    const {
      baseCardSize: { width, height },
    } = this.cardConfig;

    let [sourceX, sourceY] = [path.source.x, path.source.y];
    let [targetX, targetY] = [path.target.x, path.target.y];

    return (
      "M" +
      sourceX +
      "," +
      (sourceY + height / 2) +
      " V" +
      (targetY + sourceY) / 2 +
      " H" +
      targetX +
      " V" +
      (targetY - height / 2)
    );
  }

  initElbow(curNode) {
    const { baseCardSize } = this.cardConfig;
    let [sourceX, sourceY] = [curNode.x0, curNode.y0];

    // M0,124 V75 H0 V75
    return (
      "M" +
      sourceX +
      "," +
      (sourceY + baseCardSize.height / 2 + 4) +
      " V" +
      sourceY +
      " H" +
      sourceX +
      " V" +
      sourceY
    );
  }

  endElbow(curNode) {
    const { baseCardSize } = this.cardConfig;
    let [sourceX, sourceY] = [curNode.x, curNode.y];
    return (
      "M" +
      sourceX +
      "," +
      (sourceY + baseCardSize.height / 2 + 4) +
      " V" +
      sourceY +
      " H" +
      sourceX +
      " V" +
      sourceY
    );
  }

  paintCard(curNode, foldFlag = false) {
    this.nodes = this.data.getNodes();
    let transition = this.group.transition().duration(500);
    const { baseCardSize } = this.cardConfig;

    const nodeGroup = this.gNode
      .selectAll("g")
      .data(this.nodes, (d) => d.id + d.positionid);
    const nodeEnter = nodeGroup
      .enter()
      .append("g")
      .attr("class", (d) => "card-group card2" + d.id + d.positionid)
      .attr("transform", this.getNodeLoc(curNode, baseCardSize, "init")); // 不加这个卡片会从斜上方偏移进来
    this.nodeUpdate = nodeGroup
      .merge(nodeEnter)
      .transition(transition)
      .attr("fill-opacity", 1)
      .attr("stroke-opacity", 1)
      .attr("transform", (node) => this.getNodeLoc(node, baseCardSize));
    nodeGroup
      .exit()
      .transition(transition)
      .remove()
      .attr("transform", this.getNodeLoc(curNode, baseCardSize))
      .attr("stroke-opacity", 0)
      .attr("fill-opacity", 0);
    this.bindCardEvent(nodeEnter); // 这里要用nodeEnter，不能用nodeUpdate
    if (!foldFlag) {
      this.paintCardInfo();
    }
    this.syncLoc();
  }

  syncLoc() {
    this.data.getNodes().forEach((node) => {
      node.x0 = node.x;
      node.y0 = node.y;
    });
  }

  getNodeLoc(curNode, baseCardSize, type = "normal") {
    if (type === "normal") {
      return `translate( ${curNode.x - baseCardSize.width / 2}, ${curNode.y -
        baseCardSize.height / 2})`;
    } else {
      return `translate(${curNode.x0 - baseCardSize.width / 2}, ${curNode.y0 -
        baseCardSize.height / 2})`;
    }
  }

  bindCardEvent(nodeEnter) {
    nodeEnter.call(
      d3
        .drag()
        .on("end", (e, node) => {
          this.toCardDetail(node);
        })
        .filter((e) => {
          const baseVal = e.target.className && e.target.className.baseVal;
          if (baseVal !== "") {
            return baseVal.indexOf("op-icon") === -1; // 过滤掉展开折叠按钮
          } else {
            return true;
          }
        })
    );
  }

  paintCardInfo() {
    this.nodeUpdate._groups[0].map((g) => {
      const d = g.__data__;
      if (this.isInPaintView(d)) {
        if (g.childNodes.length <= 0) {
          g.innerHTML = "";
          this.paintRect(d, g);
          this.paintOtherInfo(d).map((resDom) => {
            g.append(resDom.node());
          });
          this.paintToggle(d, g);
        }
      } else {
        if (g.childNodes.length > 0) {
          //清空内容
          g.innerHTML = "";
        }
      }
    });
  }

  paintRect(node, g) {
    //定义一个线性渐变
    var defs = this.svg.append("defs");
    var linearGradient = defs
      .append("linearGradient")
      .attr("id", "linearColor")
      .attr("x1", "0%")
      .attr("y1", "0%")
      .attr("x2", "100%")
      .attr("y2", "0%");

    linearGradient
      .selectAll("stop")
      .data([
        { offset: "3%", color: "#29cbff" },
        { offset: "98%", color: "#5497ff" },
      ])
      .enter()
      .append("stop")
      .attr("offset", (d) => d.offset)
      .attr("stop-color", (d) => d.color);

    const rect = d3
      .create("svg:rect")
      .attr("class", "card-wrapper")
      .attr("width", this.cardConfig.baseCardSize.width)
      .attr("height", this.cardConfig.baseCardSize.height)
      .attr("rx", 10)
      .attr("ry", 10)
      .attr("fill-opacity", 1)
      .style(
        "fill",
        node.parent ? "#edf6fa" : "url(#" + linearGradient.attr("id") + ")"
      )
      .attr("stroke", node.parent ? "#44c6ff" : "none")
      .attr("stroke-linecap", "round")
      .attr("stroke-opacity", 1)
      .attr("stroke-width", 1)
      .attr("shape-rendering", "auto");

    rect.node()["__data__"] = node;
    g.appendChild(rect.node());
  }

  paintToggle(node, g) {
    if (!node.hasChild) return;
    const iconConfig = this.iconConfig;
    let isOpen = node.hasChild && !!node.children && node.children.length > 0;
    let iconType = isOpen ? iconConfig.toggleMinus : iconConfig.togglePlus;
    let hadIcon = d3.select("#tgBtnID" + node.id);
    let toggleBtn = d3.selectAll(".toggle" + node.id); // 不能只选择icon，数字和边框也要选中，不然会造成node没更新

    if (hadIcon["_groups"][0][0] !== null) {
      hadIcon.html(iconType.unicode).attr("fill", iconType.color);
      hadIcon.on("click", () => {
        this.toggleNode(node);
      });
      toggleBtn.on("click", () => {
        this.toggleNode(node);
      });
    } else {
      const baseCardSize = this.cardConfig.baseCardSize;
      const collapseStyles = this.getCollapseStyle();

      // 绘制折叠按钮圆形框
      let circle = d3
        .create("svg:circle")
        .attr("cx", (baseCardSize.width - collapseStyles.width) / 2)
        .attr("cy", baseCardSize.height)
        .attr("r", 8)
        .attr("fill", "#2cc6f2")
        .attr("class", "op-icon" + node.id)
        .on("click", () => {
          this.toggleNode(node);
        });

      g.appendChild(circle.node());

      let icon = d3
        .create("svg:text")
        .html(iconType.unicode)
        .attr("class", "op-icon " + iconConfig.class)
        .attr("id", "tgBtnID" + node.id)
        .attr("x", iconType.x)
        .attr("y", iconType.y)
        .attr("font-size", iconConfig.fontSize)
        .attr("height", iconConfig.fontSize)
        .attr("fill", "#fff");

      icon.on("click", () => {
        this.toggleNode(node);
      });

      icon.node()["__data__"] = node;
      g.appendChild(icon.node());
    }
  }

  toggleNode(node) {
    const isFold = node.children && node.children.length > 0;
    if (isFold) {
      this.foldData(node);
      this.paintFoldBtn(node);
    } else {
      this.openData(node);
      this.paintOpenBtn(node);
    }
    this.resetChartData();
    this.resetPaint(node, isFold, "fold");
  }

  foldData(node) {
    this.searchNodeByID(this.data, node.id + node.positionid, (item) => {
      item.children = null;
    });
  }

  openData(node) {
    this.searchNodeByID(this.data, node.id + node.positionid, (item) => {
      item.children = item._children;
    });
  }

  paintFoldBtn(node) {
    const icon = this.iconConfig.togglePlus;
    d3.select("#tgBtnID" + node.id)
      .html(icon.unicode)
      .attr("fill", icon.color)
      .attr("x", icon.x)
      .attr("y", icon.y);
  }

  paintOpenBtn(node) {
    const icon = this.iconConfig.toggleMinus;
    d3.select("#tgBtnID" + node.id)
      .html(icon.unicode)
      .attr("fill", icon.color)
      .attr("x", icon.x)
      .attr("y", icon.y);
  }

  searchNodeByID(data, id, cb) {
    let rootList = [data];
    each(rootList);

    function each(list) {
      for (let i = 0; i < list.length; i++) {
        const _id = list[i].id + list[i].positionid;
        if (_id === id) {
          cb(list[i]);
          break;
        }
        list[i].children && each(list[i].children);
      }
    }
  }

  paintOtherInfo(node) {
    let resDom = [];
    let cardModels = this.cardModel[node.cardModel];
    cardModels.map((cardModel, index) => {
      switch (cardModel.tag) {
        case "text":
          if (!cardModel.text(node)) return;
          let [dom, isEllipsis] = this.paintCardText(cardModel, node);
          if (dom) {
            resDom.push(dom);
          }
          break;
        case "img":
          const clipId = "clip" + node.id + index;
          const imgId = "img" + node.id + index;
          const [imgSize, x] = this.getImgConfig(cardModel);
          const clipPath = this.getClicPath(clipId, cardModel, imgSize, x);
          resDom.push(clipPath);
          const img = this.paintCardImg(
            cardModel,
            clipId,
            node,
            imgId,
            imgSize,
            x
          );
          resDom.push(img);
          break;
      }
    });
    return resDom;
  }

  getImgConfig(cardModel) {
    let textAnchor = cardModel.textAnchor || "start"; // start middle end
    let imgSize = Math.min(cardModel.w, cardModel.h);
    let x = cardModel.x;

    switch (textAnchor) {
      case "start":
        break;
      case "middle":
        x += (boxW - imgSize) / 2;
        break;
      case "end":
        x = boxW - imgSize;
        break;
    }
    return [imgSize, x];
  }

  paintCardImg(cardModel, clipId, node, imgId, imgSize, x) {
    let img = d3
      .create("svg:image")
      .attr("id", imgId)
      .attr("xlink:href", cardModel.url(node))
      .attr("width", imgSize)
      .attr("height", imgSize)
      .attr("x", x)
      .attr("y", cardModel.y)
      .attr("clip-path", "url(#" + clipId + ")");
    return img;
  }

  getClicPath(clipId, cardModel, imgSize, x) {
    let defs = d3.create("svg:defs");
    let rectId = "imgRect" + clipId;
    const r = imgSize / 2;
    defs
      .append("clipPath")
      .attr("id", clipId)
      .append("circle")
      .attr("id", rectId)
      .attr("cx", x + r)
      .attr("cy", cardModel.y + r)
      .attr("r", r)
      .attr("stroke", "#f00")
      .attr("stroke-width", 2);
    return defs;
  }

  paintCardText(cardModel, node) {
    var textConfig = {
      texts: cardModel.text(node)[0] || "",
      fontSize: cardModel.fontSize,
      fontHeight: FONT_HEIGHT_MAP[cardModel.fontSize],
      fontWeight: cardModel.fontWeight || 500,
      color: cardModel.color(node) || "#333",
      wMode: cardModel.wMode || "lr",
      valign: cardModel.valign || "center", // top  center  bottom
      textAnchor: cardModel.textAnchor || "start", // start  middle  end
      boxWidth: parseInt(cardModel.w + ""),
      boxHeight: parseInt(cardModel.h + ""),
      bx: cardModel.x,
      by: cardModel.y,
    };
    const { textDoms, isEllipsis } = this.getTextSvgDoms(textConfig);
    var textSvgDom = d3
      .create("svg:text")
      .attr("writing-mode", textConfig.wMode)
      .attr("fill", textConfig.color)
      .attr("xml:space", "preserve")
      .attr("text-anchor", "start")
      .attr("font-size", textConfig.fontSize)
      .attr("font-weight", textConfig.fontWeight)
      .attr("class", "cardto" + node.id);
    textDoms.map(function(textItem) {
      textSvgDom
        .append("svg:tspan")
        .text(textItem.text)
        .attr("x", textItem.x)
        .attr("y", textItem.y)
        .attr("font-family", SVG_FONT_FAMILY)
        .attr("word-spacing", 0)
        .attr("letter-spacing", 0);
    });
    return [textSvgDom, isEllipsis];
  }

  getTextSvgDoms(textConfig) {
    let textParams = this.computedLinesNum(textConfig);
    let textDoms = [];
    let currTexts = textConfig.texts;
    for (let lineNO = 1; lineNO <= textParams.lineNums; lineNO++) {
      let res = this.getLineTextInfo(currTexts, textConfig, textParams);
      if (textConfig.wMode === "tb") {
        textDoms.push({
          text: res.txt,
          x: textConfig.bx + (lineNO - 1 / 2) * textConfig.fontSize,
          y: textConfig.by,
          len: res.txtWidth,
        });
      } else {
        textDoms.push({
          text: res.txt,
          x: textConfig.bx,
          y: textConfig.by + lineNO * textConfig.fontSize,
          len: res.txtWidth,
        });
      }
      currTexts = res.restText;
      if (currTexts.length == 0) {
        break;
      }
    }
    textDoms.map((txtItem) => {
      const [shiftX, shiftY] = this.computedLoc(
        textConfig,
        txtItem.len,
        textDoms.length
      );
      txtItem.x += shiftX;
      txtItem.y += shiftY;
    });
    let isEllipsis = currTexts.length > 0;
    if (isEllipsis && textDoms.length > 0) {
      let txt = textDoms[textDoms.length - 1].text;
      textDoms[textDoms.length - 1].text = txt.substr(0, txt.length - 1) + "…";
    }
    return { textDoms, isEllipsis };
  }

  computedLoc(textConfig, len, realLines) {
    let shiftY = 0;
    let shiftX = 0;
    if (textConfig.wMode === "tb") {
      // 横向偏移
      shiftX = (textConfig.boxWidth - realLines * textConfig.fontSize) / 2;
      // 纵向偏移
      switch (textConfig.valign) {
        case "center":
          shiftY = (textConfig.boxHeight - len) / 2;
          break;
        case "bottom":
          shiftY = textConfig.boxHeight - len;
          break;
      }
    } else {
      // 纵向偏移
      switch (textConfig.valign) {
        case "center":
          shiftY =
            (textConfig.boxHeight - realLines * textConfig.fontHeight) / 2;
          break;
        case "bottom":
          shiftY = textConfig.boxHeight - realLines * textConfig.fontHeight;
          break;
      }
      // 横向偏移
      switch (textConfig.textAnchor) {
        case "middle":
          shiftX = (textConfig.boxWidth - len) / 2;
          break;
        case "end":
          shiftX = textConfig.boxWidth - len;
          break;
      }
    }
    return [shiftX, shiftY];
  }

  computedLinesNum(params) {
    let textLineWidth = 0;
    let lineNums = 0;
    if (params.wMode === "tb") {
      textLineWidth = params.boxHeight;
      lineNums = Math.floor(params.boxWidth / params.fontSize);
    } else {
      textLineWidth = params.boxWidth;
      lineNums = Math.floor(params.boxHeight / params.fontHeight);
    }
    return {
      textLineWidth,
      lineNums,
    };
  }

  getLineTextInfo(currTexts, textConfig, textParams) {
    let txt = "";
    let step = 0;
    let txtWidth = 2;
    for (; step < currTexts.length; step++) {
      let charWidth = this.getChartWidth(
        currTexts[step],
        textConfig.fontSize,
        textConfig.fontWeight,
        textConfig.wMode
      );
      if (txtWidth + charWidth <= textParams.textLineWidth) {
        txt += currTexts[step];
        txtWidth += charWidth;
      } else {
        break;
      }
    }
    return {
      txt,
      txtWidth,
      restText: currTexts.slice(step),
    };
  }

  getChartWidth(chartCode = "", fontSize = 14, fontWeight = 500, wMode = "tb") {
    let width = 0;
    if (chartCode.charCodeAt(0) > 255) {
      width = fontSize;
    } else {
      let cwType = "S" + fontSize + "F" + (fontWeight >= 600 ? "6" : "N");
      switch (cwType) {
        case "S12FN":
          width = CHART_WIDTH_S12FN[chartCode];
          break;
        case "S14FN":
          width = CHART_WIDTH_S14FN[chartCode];
          break;
        case "S16FN":
          width = CHART_WIDTH_S16FN[chartCode];
          break;
        case "S12F6":
          width = CHART_WIDTH_S12F6[chartCode];
          break;
        case "S14F6":
          width = CHART_WIDTH_S14F6[chartCode];
          break;
        case "S16F6":
          width = CHART_WIDTH_S16F6[chartCode];
          break;
      }
    }
    return width;
  }

  extremeNodeLoc() {
    this.maxLoc = {
      x: -Infinity,
      y: -Infinity,
    };
    this.minLoc = {
      x: Infinity,
      y: Infinity,
    };

    this.data.getNodes().forEach((node) => {
      this.maxLoc.x = Math.max(node.x, this.maxLoc.x);
      this.maxLoc.y = Math.max(node.y, this.maxLoc.y);

      this.minLoc.x = Math.min(node.x, this.minLoc.x);
      this.minLoc.y = Math.min(node.y, this.minLoc.y);
    });
    this.relativeMaxLoc.x = Math.max(this.maxLoc.x, Math.abs(this.minLoc.x));
    this.relativeMaxLoc.y = Math.max(this.maxLoc.y, Math.abs(this.minLoc.y));

    let newSvgSizeWidth = Math.max(
      this.relativeMaxLoc.x * 2 + this.cardConfig.baseCardSize.width * 2,
      this.svgWrapperSize.width
    );
    this.isRelativeMaxChange = newSvgSizeWidth !== this.svgSize.width;
  }
}
