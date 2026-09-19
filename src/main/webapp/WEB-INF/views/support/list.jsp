<%@ page pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<div class="support-heading"><div><p class="eyebrow">${adminView ? 'CUSTOMER CARE' : 'MY SUPPORT'}</p><h1><c:out value="${pageTitle}"/></h1><p class="muted">${adminView ? '먼저 확인할 문의를 찾고, 고객에게 보낼 답변을 완성하세요.' : '문의와 답변을 검색하고 처리 상태를 확인하세요.'}</p></div>
<c:if test="${not adminView}"><a class="support-button" href="${pageContext.request.contextPath}/account/inquiry/new">새 문의 작성 ↗</a></c:if></div>
<c:if test="${adminView}">
<div class="support-metrics" aria-label="전체 문의 처리 현황">
<a class="metric-card" href="?status=OPEN&amp;sort=oldest"><span>답변 대기</span><strong><fmt:formatNumber value="${stats.openCount}"/></strong><small>접수된 순서로 확인 →</small></a>
<a class="metric-card" href="?attention=overdue&amp;sort=oldest"><span>24시간 이상 대기</span><strong><fmt:formatNumber value="${stats.overdueCount}"/></strong><small>오래 기다린 고객부터 →</small></a>
<a class="metric-card" href="?attention=ai&amp;sort=oldest"><span>AI 확인 필요</span><strong><fmt:formatNumber value="${stats.aiAttentionCount}"/></strong><small>실패·중단된 작업 확인 →</small></a>
<a class="metric-card" href="?attention=today"><span>오늘 답변 처리</span><strong><fmt:formatNumber value="${stats.answeredTodayCount}"/></strong><small>한국 시간 기준 →</small></a>
</div><p class="help">요약은 검색 조건과 관계없이 전체 문의를 기준으로 합니다.</p>
<c:if test="${not aiEnabled}"><div class="support-notice">AI가 연결되지 않았습니다. 문의 접수와 직접 답변은 정상적으로 사용할 수 있습니다.</div></c:if>
</c:if>
<form method="get" class="support-panel support-filter-grid">
<div class="search-field"><label for="keyword">문의 검색</label><input type="search" id="keyword" name="keyword" maxlength="100" value="<c:out value='${searchFilter.keyword}'/>" placeholder="제목, 내용, 상품명, 주문번호"></div>
<div><label for="status">처리 상태</label><select id="status" name="status"><option value="">전체</option><option value="OPEN" ${searchFilter.status == 'OPEN' ? 'selected' : ''}>답변 대기</option><option value="ANSWERED" ${searchFilter.status == 'ANSWERED' ? 'selected' : ''}>답변 완료</option></select></div>
<div><label for="category">문의 유형</label><select id="category" name="category"><option value="">전체</option><option value="PRODUCT" ${searchFilter.category == 'PRODUCT' ? 'selected' : ''}>상품 정보</option><option value="ORDER" ${searchFilter.category == 'ORDER' ? 'selected' : ''}>주문·배송</option><option value="RETURN" ${searchFilter.category == 'RETURN' ? 'selected' : ''}>교환·반품</option><option value="OTHER" ${searchFilter.category == 'OTHER' ? 'selected' : ''}>기타</option></select></div>
<div><label for="sort">정렬</label><select id="sort" name="sort"><option value="newest" ${searchFilter.sort == 'newest' ? 'selected' : ''}>최신 접수순</option><option value="oldest" ${searchFilter.sort == 'oldest' ? 'selected' : ''}>오래된 접수순</option></select></div>
<c:if test="${adminView}"><div><label for="attention">확인 항목</label><select id="attention" name="attention"><option value="">전체</option><option value="overdue" ${searchFilter.attention == 'overdue' ? 'selected' : ''}>24시간 이상 대기</option><option value="ai" ${searchFilter.attention == 'ai' ? 'selected' : ''}>AI 확인 필요</option><option value="today" ${searchFilter.attention == 'today' ? 'selected' : ''}>오늘 답변 처리</option></select></div></c:if>
<div class="filter-actions"><button class="support-button" type="submit">검색</button><a href="${pageContext.request.contextPath}${adminView ? '/admin/inquiries' : '/account/inquiries'}">초기화</a></div>
</form>
<p class="result-count" role="status">검색 결과 <strong><fmt:formatNumber value="${searchPage.totalCount}"/></strong>건</p>
<div class="inquiry-list">
<c:forEach var="q" items="${inquiries}">
<c:url var="detailUrl" value="${adminView ? '/admin/inquiry' : '/account/inquiry'}"><c:param name="id" value="${q.id}"/></c:url>
<a class="inquiry-card" href="<c:out value='${detailUrl}'/>"><div><span class="status-pill ${q.status == 'ANSWERED' ? 'answered' : ''}">${q.status == 'ANSWERED' ? '답변 완료' : '답변 대기'}</span><h2><c:out value="${q.title}"/></h2><p class="muted"><c:out value="${q.productName}"/> <c:out value="${q.orderNumber}"/></p></div><div class="inquiry-date"><fmt:formatDate value="${q.createdAt}" pattern="yyyy.MM.dd HH:mm"/><span aria-hidden="true"> ↗</span></div></a>
</c:forEach>
<c:if test="${empty inquiries}"><div class="empty-state"><h2>조건에 맞는 문의가 없습니다.</h2><p>검색어나 필터를 바꾸거나 초기화해 보세요.</p></div></c:if>
</div>
<nav class="support-pagination" aria-label="문의 페이지">
<c:if test="${searchPage.hasPrevious}"><c:url var="prevUrl" value=""><c:param name="page" value="${searchPage.page-1}"/><c:param name="status" value="${searchFilter.status}"/><c:param name="category" value="${searchFilter.category}"/><c:param name="keyword" value="${searchFilter.keyword}"/><c:param name="sort" value="${searchFilter.sort}"/><c:param name="attention" value="${searchFilter.attention}"/></c:url><a href="<c:out value='${prevUrl}'/>">← 이전</a></c:if>
<span><c:out value="${searchPage.page}"/> / <c:out value="${searchPage.totalPages}"/> 페이지</span>
<c:if test="${searchPage.hasNext}"><c:url var="nextUrl" value=""><c:param name="page" value="${searchPage.page+1}"/><c:param name="status" value="${searchFilter.status}"/><c:param name="category" value="${searchFilter.category}"/><c:param name="keyword" value="${searchFilter.keyword}"/><c:param name="sort" value="${searchFilter.sort}"/><c:param name="attention" value="${searchFilter.attention}"/></c:url><a href="<c:out value='${nextUrl}'/>">다음 →</a></c:if>
</nav></main></body></html>
