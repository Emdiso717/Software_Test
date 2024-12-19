<template>
    <el-scrollbar height="100%" style="width: 100%; height: 100%; ">
        <div style="margin-top: 20px; margin-left: 40px; font-size: 2em; font-weight: bold; ">图书管理</div>
        <!--书籍查询 -->
        <div>
            <el-button style="margin-left: 40px;margin-top: 10px;" type="primary" :icon="Search"
                @click="this.searchvisible = true, isshow = true, this.tableData = []">书籍查询</el-button>
            <el-button style="margin-left: 40px;margin-top: 10px;" type="success"
                @click="borrowvisible = true"><el-icon>
                    <Collection />
                </el-icon>书籍借阅</el-button>
            <el-button style="margin-left: 40px;margin-top: 10px;" type="warning"
                @click="returnvisible = true"><el-icon>
                    <Reading />
                </el-icon>书籍归还</el-button>
            <el-button style="margin-left: 40px;margin-top: 10px;" type="info" @click="inbookvisible = true"><el-icon>
                    <DocumentAdd />
                </el-icon>入库新书</el-button>
            <!--查询条件筛选-->
            <div v-if="searchvisible" style="margin:0 auto;width: 80%;margin-top: 20px; font-weight: bold;">
                <el-card style="margin:0 auto;height: auto;width: 80%;">
                    <template #header>
                        <div style="margin-left: 40%;">
                            <p style="font-size: 1.5em; font-weight: bold;">查询条件筛选</p>
                        </div>
                    </template>
                    <div>
                        <span style="margin-right:1%;">书籍种类</span>
                        <el-input v-model="this.searchbooks.category" style="width: 35%" placeholder="输入书籍种类"
                            prefix-icon="Search" clearable />
                        <span style="margin-left:8%;margin-right:1%;">书籍标题</span>
                        <el-input v-model="this.searchbooks.title" style="width: 35%" placeholder="输入书籍标题"
                            prefix-icon="Search" clearable />
                    </div>
                    <div style="margin-top:2.5%;">
                        <span style="margin-right:1%;">出版社名称</span>
                        <el-input v-model="this.searchbooks.press" style="width: 35%" placeholder="输入出版社名"
                            prefix-icon="Search" clearable />
                        <span style="margin-left:8%;margin-right:1%;">书籍作者</span>
                        <el-input v-model="this.searchbooks.author" style="width: 35%" placeholder="输入作者姓名"
                            prefix-icon="Search" clearable />
                    </div>
                    <div style="margin-top:2.5%;">
                        <span style="margin-right:1%;">年代范围</span>
                        <el-date-picker v-model="this.searchbooks.minpubilshyear" style="width: 15%" type="year"
                            placeholder="选择年代范围" />
                        <span style="margin-right:1%;margin-left: 1%">——</span>
                        <el-date-picker v-model="this.searchbooks.maxpubilshyear" style="width:15%" type="year"
                            placeholder="选择年代范围" />
                        <span style="margin-right:1%;margin-left: 8%;">价格范围</span>
                        <el-input v-model="this.searchbooks.minprice" style="width: 15%" placeholder="选择价格范围"
                            prefix-icon="Search" clearable />
                        <span style="margin-right:1%;margin-left: 1%;">——</span>
                        <el-input v-model="this.searchbooks.maxprice" style="width: 15%" placeholder="选择价格范围"
                            prefix-icon="Search" clearable />
                    </div>
                    <div style="margin-top:2.5%;">
                        <span style="margin-right:1%;">排列顺序</span>
                        <el-select v-model="searchbooks.sortby" placeholder="Select" style="width: 20%">
                            <el-option v-for="item in searchsortby" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                        <span style="margin-right:1%;"></span>
                        <el-select v-model="searchbooks.sortorder" placeholder="Select" style="width: 20%">
                            <el-option v-for="item in searchorder" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                    </div>
                    <template #footer>
                        <div style="margin-bottom:1%;float: right;">
                            <el-button type="primary" :icon="Search" @click="Quarybooks">查询</el-button>
                            <el-button
                                @click="searchvisible = false, searchbooks.category = '', searchbooks.title = '', searchbooks.press = '',
                                    searchbooks.minpubilshyear = '', searchbooks.maxpubilshyear = '', searchbooks.minprice = '', searchbooks.maxprice = '',
                                    searchbooks.author = '', searchbooks.sortby = 'book_id', searchbooks.sortorder = 'ASC'">取消</el-button>
                        </div>
                    </template>
                </el-card>
            </div>
        </div>
        <!--借书卡-->
        <div v-if="borrowvisible" style="margin:0 auto;width: 80%;margin-top: 20px; font-weight: bold;">
            <el-card style="margin:0 auto;height: auto;width: 80%;">
                <template #header>
                    <div style="margin-left: 40%;">
                        <p style="font-size: 1.5em; font-weight: bold;">借书登记</p>
                    </div>
                </template>
                <div>
                    <span style="margin-right:1%;">借书证号</span>
                    <el-input v-model="borrowbook.cardid" style="width: 35%" placeholder="输入借书证号" prefix-icon="Postcard"
                        clearable />
                    <span style="margin-left:8%;margin-right:1%;">输入书籍编号</span>
                    <el-input v-model="borrowbook.bookid" style="width: 35%" placeholder="输入书籍编号" prefix-icon="Notebook"
                        clearable />
                </div>
                <div style="margin-top:2.5%;">
                    <span style="margin-right:1%">选择借阅日期</span>
                    <el-date-picker v-model="borrowbook.borrowtime" type="datetime" placeholder="选择借阅日期"
                        format="YYYY/MM/DD HH:mm" date-format="YYYY/MM/DD" time-format="HH:mm" value-format="x" />
                </div>
                <template #footer>
                    <div style="margin-bottom:1%;float: right;">
                        <el-button type="success" @click="BorrowBook"
                            :disabled="borrowbook.cardid.length === 0 || borrowbook.bookid.length === 0 || borrowbook.borrowtime.length === 0"><el-icon>
                                <DocumentChecked />
                            </el-icon>借阅</el-button>
                        <el-button
                            @click="borrowvisible = false, borrowbook.bookid = '', borrowbook.cardid = '', borrowbook.borrowtime = ''">取消</el-button>
                    </div>
                </template>

            </el-card>
        </div>
        <!--还书界面-->
        <div v-if="returnvisible" style="margin:0 auto;width: 80%;margin-top: 20px; font-weight: bold;">
            <el-card style="margin:0 auto;height: auto;width: 80%;">
                <template #header>
                    <div style="margin-left: 40%;">
                        <p style="font-size: 1.5em; font-weight: bold;">还书登记</p>
                    </div>
                </template>
                <div>
                    <span style="margin-right:1%;">借书证号</span>
                    <el-input v-model="returnbook.cardid" style="width: 35%" placeholder="输入借书证号" prefix-icon="Postcard"
                        clearable />
                    <span style="margin-left:8%;margin-right:1%;">输入书籍编号</span>
                    <el-input v-model="returnbook.bookid" style="width: 35%" placeholder="输入书籍编号" prefix-icon="Notebook"
                        clearable />
                </div>
                <div style="margin-top:2.5%;">
                    <span style="margin-right:1%">选择归还日期</span>
                    <el-date-picker v-model="returnbook.returntime" type="datetime" placeholder="选择借阅日期"
                        format="YYYY/MM/DD HH:mm" date-format="YYYY/MM/DD" time-format="HH:mm" value-format="x" />
                </div>
                <template #footer>
                    <div style="margin-bottom:1%;float: right;">
                        <el-button type="warning" @click="ReturnBook"
                            :disabled="returnbook.cardid.length === 0 || returnbook.bookid.length === 0 || returnbook.returntime.length === 0"><el-icon>
                                <DocumentChecked />
                            </el-icon>确认归还</el-button>
                        <el-button
                            @click="returnvisible = false, returnbook.bookid = '', returnbook.cardid = '', returnbook.returntime = ''">取消</el-button>
                    </div>
                </template>
            </el-card>
        </div>
        <!--入库新书-->
        <div>
            <el-dialog v-model="inbookvisible" width="80%" align-center>
                <template #header>
                    <div style="margin-left: 50%;">
                        <p style="font-size: 1.5em; font-weight: bold;">新书入库</p>
                    </div>
                </template>
                <el-button style="margin-left: 40px;margin-top: 10px;" type="success"
                    @click="newvisible = true"><el-icon>
                        <DocumentAdd />
                    </el-icon>添加新书</el-button>
                <el-button style="margin-left: 40px;margin-top: 10px;" type="danger" @click="newbooks = []"><el-icon>
                        <Delete />
                    </el-icon>清空列表</el-button>
                <el-upload :action='uploadActionUrl' :on-success="handleSuccess" :limit="1" :accept="'.json'"
                    ref="uploadRef">
                    <el-button slot="trigger" type="primary" style="margin-left: 40px;margin-top:
                        10px;"><el-icon>
                            <Document />
                        </el-icon>选取文件</el-button>
                    <div slot="tip" class="el-upload__tip">只能上传JSON文件</div>
                </el-upload>
                <el-dialog v-model="newvisible" :title="'书籍信息填写'" width="30%" align-center>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        书籍种类：
                        <el-input v-model="this.newbook.category" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        标题：
                        <el-input v-model="this.newbook.title" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        出版社：
                        <el-input v-model="this.newbook.press" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        出版年份：
                        <el-input v-model="this.newbook.publishyear" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        作者：
                        <el-input v-model="this.newbook.author" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        价格：
                        <el-input v-model="this.newbook.price" style="width: 12.5vw;" clearable />
                    </div>
                    <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                        库存：
                        <el-input v-model="this.newbook.stock" style="width: 12.5vw;" clearable />
                    </div>
                    <template #footer>
                        <span class="dialog-footer">
                            <el-button @click="newvisible = false, newbook = {}">取消</el-button>
                            <el-button type="primary" @click="newvisible = false, newbooks.push(newbook), newbook = {}"
                                :disabled="this.newbook.category === '' || this.newbook.stock === '' || this.newbook.title === '' || this.newbook.publishyear === '' || this.newbook.author === '' || this.newbook.price === ''">确定</el-button>
                        </span>
                    </template>
                </el-dialog>
                <div>
                    <el-table :data="newbooks" height="400" :table-layout="'auto'"
                        style="width: 95%; margin-left: 3%; margin-top: 30px; margin-right: 3%; max-width: 80vw;font-weight: bold;">
                        <el-table-column type="index" width="50" />
                        <el-table-column prop="category" label="书籍种类" />
                        <el-table-column prop="title" label="标题" />
                        <el-table-column prop="press" label="出版社" />
                        <el-table-column prop="publishyear" label="出版年份" />
                        <el-table-column prop="author" label="作者" />
                        <el-table-column prop="price" label="价格" />
                        <el-table-column prop="stock" label="库存" />
                        <el-table-column label="操作">
                            <!--调整书籍按钮-->
                            <template #default="scope">
                                <el-button type="primary" :icon="Edit"
                                    @click="newadv = true, changerow = scope.$index">修改</el-button>
                                <el-button type="danger" :icon="Delete"
                                    @click="newbooks.splice(scope.$index, 1)">删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                <template #footer>
                    <div style="margin-bottom:1%;float: right;">
                        <el-button type="success" @click="storeBook"><el-icon>
                                <DocumentChecked />
                            </el-icon>确认入库</el-button>
                        <el-button @click="clearFiles">取消</el-button>
                    </div>
                </template>
            </el-dialog>
            <el-dialog v-model="newadv" :title="'书籍信息修改'" width="30%" align-center>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    书籍种类：
                    <el-input v-model="this.newbooks[changerow].category" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    标题：
                    <el-input v-model="this.newbooks[changerow].title" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    出版社：
                    <el-input v-model="this.newbooks[changerow].press" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    出版年份：
                    <el-input v-model="this.newbooks[changerow].publishyear" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    作者：
                    <el-input v-model="this.newbooks[changerow].author" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    价格：
                    <el-input v-model="this.newbooks[changerow].price" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    库存：
                    <el-input v-model="this.newbooks[changerow].stock" style="width: 12.5vw;" clearable />
                </div>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="newadv = false">取消</el-button>
                        <el-button type="primary" @click="newadv = false"
                            :disabled="this.newbooks[changerow].category === '' || this.newbooks[changerow].stock === '' || this.newbooks[changerow].title === '' || this.newbooks[changerow].publishyear === '' || this.newbooks[changerow].author === '' || this.newbooks[changerow].price === ''">确定</el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
        <!--书籍显示表格-->
        <div>
            <el-table v-if="isshow" :data="tableData" height="600" :table-layout="'auto'" stripe
                :row-key="row => row.id"
                style="width: 100%; margin-left: 50px; margin-top: 30px; margin-right: 50px; max-width: 80vw;font-weight: bold;">
                <el-table-column prop="id" label="书籍编号" />
                <el-table-column prop="category" label="书籍种类" />
                <el-table-column prop="title" label="标题" />
                <el-table-column prop="press" label="出版社" />
                <el-table-column prop="publishyear" label="出版年份" />
                <el-table-column prop="author" label="作者" />
                <el-table-column prop="price" label="价格" />
                <el-table-column prop="stock" label="库存" />
                <el-table-column label="操作">
                    <!--调整书籍按钮-->
                    <template #default="scope">
                        <el-button type="primary" :icon="Edit"
                            @click="adjestvisible = true, adjestbook.id = scope.row.id, adjestbook.category = scope.row.category,
                                adjestbook.title = scope.row.title, adjestbook.press = scope.row.press, adjestbook.publishyear = scope.row.publishyear,
                                adjestbook.author = scope.row.author, adjestbook.price = scope.row.price, adjestbook.stock = scope.row.stock">修改</el-button>
                        <el-button type="danger" :icon="Delete"
                            @click="deletevisiable = true, removeid = scope.row.id">删除</el-button>
                        <el-button type="info"
                            @click="modifystockvisiable = true, modifystock.id = scope.row.id"><el-icon>
                                <DocumentAdd />
                            </el-icon><span style="margin-left: 1%;">调整库存</span></el-button>

                    </template>
                </el-table-column>
            </el-table>
        </div>
        <!--调整界面-->
        <div>
            <el-dialog v-model="adjestvisible" :title="'修改书籍信息(书籍ID: ' + this.adjestbook.id + ')'" width="30%"
                align-center>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    书籍种类：
                    <el-input v-model="this.adjestbook.category" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    标题：
                    <el-input v-model="this.adjestbook.title" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw;   font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    出版社：
                    <el-input v-model="this.adjestbook.press" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    出版年份：
                    <el-input v-model="this.adjestbook.publishyear" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    作者：
                    <el-input v-model="this.adjestbook.author" style="width: 12.5vw;" clearable />
                </div>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    价格：
                    <el-input-number v-model="this.adjestbook.price" style="width: 12.5vw;" :precision="2"
                        :step="0.1" />
                </div>

                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="adjestvisible = false">取消</el-button>
                        <el-button type="primary" @click="modifybook"
                            :disabled="this.adjestbook.category === '' || this.adjestbook.title === '' || this.adjestbook.publishyear === '' || this.adjestbook.author === '' || this.adjestbook.price === ''">确定</el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
        <!--删除界面-->
        <div>
            <el-dialog v-model="deletevisiable" title="删除书籍" width="30%">
                <span>确定删除<span style="font-weight: bold;">{{ removeid }}号书籍</span>吗？</span>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button @click="deletevisiable = false">取消</el-button>
                        <el-button type="danger" @click="removebook">
                            删除
                        </el-button>
                    </span>
                </template>
            </el-dialog>
        </div>
        <!--修改库存-->
        <div>
            <el-dialog v-model="modifystockvisiable" title="修改库存" width="30%">
                <span>修改<span style="font-weight: bold;">{{ modifystock.id }}号书的库存</span>吗？</span>
                <div style="margin-left: 2vw; font-weight: bold; font-size: 1rem; margin-top: 20px; ">
                    调整库存
                    <el-select v-model="modifystock.order" placeholder="Select" style="width: 20%">
                        <el-option v-for="item in modiftstockorder" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                    <el-input-number v-model="modifystock.stock" :min=0 />
                </div>
                <template #footer>
                    <span class="dialog-footer">
                        <el-button
                            @click="modifystockvisiable = false, modifystock.stock = 0, this.modifystock.order = '1'">取消</el-button>
                        <el-button type="primary" @click="modifyStock">
                            确定修改
                        </el-button>
                    </span>
                </template>
            </el-dialog>
        </div>

    </el-scrollbar>
</template>

<script>
import axios from 'axios';
import { Delete, Edit, Search, Postcard, Notebook, DocumentAdd } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
export default {
    data() {
        return {
            uploadActionUrl: 'http://localhost:8000/book/u',
            changerow: 0,
            newbooks: [],
            newbook: {
                category: '',
                title: '',
                press: '',
                publishyear: '',
                author: '',
                price: '',
                stock: '',
            },
            borrowbook: {
                cardid: '',
                bookid: '',
                borrowtime: ''
            },
            returnbook: {
                cardid: '',
                bookid: '',
                returntime: ''
            },
            searchbooks: { // 借书证列表
                category: '',
                title: '',
                press: '',
                minpubilshyear: '',
                maxpubilshyear: '',
                author: '',
                minprice: '',
                maxprice: '',
                sortby: 'book_id',
                sortorder: 'ASC'
            },
            modifystock: {
                id: '',
                stock: 0,
                order: '1'
            },
            removeid: '',
            searchsortby: [
                {
                    label: "书籍序号",
                    value: "book_id"
                },
                {
                    label: "书籍种类",
                    value: "category"
                },
                {
                    label: "书籍标题",
                    value: "title"
                },
                {
                    label: "书籍出版社",
                    value: "press"
                },
                {
                    label: "书籍年份",
                    value: "publish_year"
                },
                {
                    label: "书籍作者",
                    value: "author"
                },
                {
                    label: "书籍价格",
                    value: "price"
                },
                {
                    label: "书籍库存",
                    value: "stock"
                }
            ],
            searchorder: [
                {
                    label: "增序",
                    value: "asc"
                },
                {
                    label: "降序",
                    value: "desc"
                }
            ],
            modiftstockorder: [
                {
                    label: "增加",
                    value: "1"
                },
                {
                    label: "减少",
                    value: "2"
                }
            ],
            tableData: [{ // 列表项
                id: '',
                category: '',
                title: '',
                press: '',
                publishyear: '',
                author: '',
                price: '',
                stock: '',
            }],
            adjestvisible: false,
            adjestbook: {
                id: '',
                category: '',
                title: '',
                press: '',
                publishyear: '',
                author: '',
                price: '',
                stock: '',
            },
            isshow: false,
            borrowvisible: false,
            returnvisible: false,
            modifystockvisiable: false,
            searchvisible: false,
            deletevisiable: false,
            inbookvisible: false,
            newvisible: false,
            newadv: false,
            Search,
            Edit,
            Delete,
            Postcard,
            Notebook,
            DocumentAdd,
            jsonData: null
        }
    },
    computed: {
    },
    methods: {
        clearFiles() {
            this.$refs.uploadRef.clearFiles()
            this.inbookvisible = false
            this.newbooks = []
        },
        handleSuccess(response, file) {
            // 处理文件上传成功后的逻辑
            console.log('上传成功:', response, file);
            const reader = new FileReader();
            reader.onload = (e) => {
                try {
                    // 将读取的内容转换为JSON对象
                    this.newbooks = JSON.parse(e.target.result);
                    ElMessage.success("文件上传成功！")
                } catch (error) {
                    ElMessage.error('JSON解析错误:', error);
                }
            };
            reader.readAsText(file.raw);
        },
        storeBook() {
            axios.post("/book/n", this.newbooks)
                .then(response => {
                    if (response.data == "success") {
                        ElMessage.success("书籍插入成功！")
                    }
                    else {
                        ElMessage.error("书籍插入失败：" + response.data)
                    }
                    this.inbookvisible = false
                    this.Quarybooks()
                    this.newbooks = []
                })
            this.$refs.uploadRef.clearFiles()
        },
        modifybook() {
            axios.post("/book/m",
                {
                    id: this.adjestbook.id,
                    category: this.adjestbook.category,
                    title: this.adjestbook.title,
                    press: this.adjestbook.press,
                    publishyear: this.adjestbook.publishyear,
                    author: this.adjestbook.author,
                    price: this.adjestbook.price,
                })
                .then(response => {
                    if (response.data == "success") {
                        ElMessage.success("书籍信息调整成功！")
                    }
                    else {
                        ElMessage.error("书籍信息调整失败：" + response.data)
                    }
                    this.adjestvisible = false
                    this.Quarybooks()
                })
        },
        removebook() {
            axios.delete("/book", { params: { bookid: this.removeid } })
                .then(response => {
                    this.deletevisiable = false
                    if (response.data == "success") {
                        ElMessage.success("书籍删除成功")
                    } else {
                        ElMessage.error("书籍删除失败：" + response.data)
                    }
                    this.Quarybooks()
                })
        },
        modifyStock() {
            axios.post("/book/s",
                {
                    id: this.modifystock.id,
                    stock: this.modifystock.stock,
                    order: this.modifystock.order
                })
                .then(response => {
                    if (response.data == "success") {
                        ElMessage.success("库存调整成功！")
                    }
                    else {
                        ElMessage.error("库存调整失败：" + response.data)
                    }
                    this.modifystock.stock = 0
                    this.modifystock.order = '1'
                    this.modifystockvisiable = false
                    this.Quarybooks()
                })

        },
        BorrowBook() {
            axios.post("/book/b",
                {
                    cardid: this.borrowbook.cardid,
                    bookid: this.borrowbook.bookid,
                    borrowtime: this.borrowbook.borrowtime
                })
                .then(response => {
                    if (response.data == "success") {
                        ElMessage.success("书籍借阅成功！")
                    }
                    else {
                        ElMessage.error("书籍借阅失败：" + response.data)
                    }
                    this.Quarybooks()

                })
        },
        ReturnBook() {
            axios.post("/book/r",
                {
                    cardid: this.returnbook.cardid,
                    bookid: this.returnbook.bookid,
                    returntime: this.returnbook.returntime
                })
                .then(response => {
                    if (response.data == "success") {
                        ElMessage.success("书籍归还成功！")
                    }
                    else {
                        ElMessage.error("书籍归还失败：" + response.data)
                    }
                    this.Quarybooks()
                })

        },
        Quarybooks() {
            this.tableData = []
            let response = axios.get('/book/q', {
                params: {
                    category: this.searchbooks.category, title: this.searchbooks.title,
                    press: this.searchbooks.press, minpubilshyear: this.searchbooks.minpubilshyear,
                    maxpubilshyear: this.searchbooks.maxpubilshyear, author: this.searchbooks.author,
                    minprice: this.searchbooks.minprice, maxprice: this.searchbooks.maxprice,
                    sortby: this.searchbooks.sortby, sortorder: this.searchbooks.sortorder
                }
            }).then(response => {
                let books = response.data
                books.forEach(book => {
                    this.tableData.push(book)
                })
            });
            this.isshow = true
        }
    }
}
</script>


<style>
.centered-header {
    display: flex;
    justify-content: center;
}

.demo-date-picker {
    display: flex;
    width: 100%;
    padding: 0;
    flex-wrap: wrap;
}

.demo-date-picker .block {
    padding: 30px 0;
    text-align: center;
    border-right: solid 1px var(--el-border-color);
    flex: 1;
}

.demo-date-picker .demonstration {
    display: block;
    color: var(--el-text-color-secondary);
    font-size: 14px;
    margin-bottom: 20px;
}
</style>