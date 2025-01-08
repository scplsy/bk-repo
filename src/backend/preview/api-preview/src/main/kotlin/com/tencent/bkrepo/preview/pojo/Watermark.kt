/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tencent.bkrepo.preview.pojo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("水印信息")
data class Watermark(
    @ApiModelProperty("水印内容")
    var watermarkTxt: String? = null,
    @ApiModelProperty("水印x轴间隔")
    var watermarkXSpace: String? = null,
    @ApiModelProperty("水印y轴间隔")
    var watermarkYSpace: String? = null,
    @ApiModelProperty("水印字体")
    var watermarkFont: String? = null,
    @ApiModelProperty("水印字体大小")
    var watermarkFontsize: String? = null,
    @ApiModelProperty("水印颜色")
    var watermarkColor: String? = null,
    @ApiModelProperty("水印透明度")
    var watermarkAlpha: String? = null,
    @ApiModelProperty("水印宽度")
    var watermarkWidth: String? = null,
    @ApiModelProperty("水印高度")
    var watermarkHeight: String? = null,
    @ApiModelProperty("水印倾斜度数")
    var watermarkAngle: String? = null,
)